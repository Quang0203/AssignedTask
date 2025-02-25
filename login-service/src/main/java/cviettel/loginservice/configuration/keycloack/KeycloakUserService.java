package cviettel.loginservice.configuration.keycloack;

import cviettel.loginservice.configuration.keycloack.exception.CustomKeycloakException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.entity.User;
import cviettel.loginservice.entity.UserSession;
import cviettel.loginservice.repository.UserRepository;
import cviettel.loginservice.repository.UserSessionRepository;
import cviettel.loginservice.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

@Service
public class KeycloakUserService {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${env.username}")
    private String username;

    @Value("${env.password}")
    private String password;

    private final PasswordEncoder encoder;

    private final UserService userService;

//    private final TokenManager tokenManager;

    private final UserRepository userRepository;

    private final UserSessionRepository userSessionRepository;

    // Số lần refresh tối đa cho phép
    private static final int MAX_REFRESH_COUNT = 3;

    public KeycloakUserService(UserService userService, UserRepository userRepository, PasswordEncoder encoder,  UserSessionRepository userSessionRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.encoder = encoder;
//        this.tokenManager = tokenManager;
        this.userSessionRepository = userSessionRepository;
    }

    // Phương thức đăng nhập: sau khi lấy token thành công, cập nhật phiên đăng nhập của user
    @Transactional
    public LoginResponse getToken(String username, String password) {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        String requestBody = String.format("client_id=%s&username=%s&password=%s&grant_type=password", clientId, username, password);
        ResponseEntity<String> response = sendPostRequest(url, requestBody);
        System.out.println("Response at getToken(): " + response);
        LoginResponse loginResponse = parseTokenResponse(response.getBody());

        // Cập nhật session: chỉ cho phép một phiên cho mỗi user
        Optional<User> userOptional = userRepository.findByEmail(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Kiểm tra xem user có session cũ không
            Optional<UserSession> oldSession = userSessionRepository.findByUserId(user.getUserId());
            if (oldSession.isPresent()) {
                // Thu hồi token cũ trước khi xóa session
                String oldRefreshToken = oldSession.get().getRefreshToken();
                if (oldRefreshToken != null) {
                    revokeToken(oldRefreshToken);
                }
                userSessionRepository.deleteByUserId(user.getUserId());
            }

            // Lưu session mới với access và refresh token, khởi tạo số lần refresh = 0
            UserSession session = new UserSession(user.getUserId(), loginResponse.getAccessToken(), loginResponse.getRefreshToken(), Instant.now(), 0);
            userSessionRepository.save(session);
        } else {
            System.out.println("User with email " + username + " not found in local repository.");
        }

        return loginResponse;
    }

    // Phương thức làm mới token: cập nhật lại phiên đăng nhập với token mới và đếm số lần refresh
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        String requestBody = String.format("client_id=%s&refresh_token=%s&grant_type=refresh_token", clientId, refreshToken);
        ResponseEntity<String> response = sendPostRequest(url, requestBody);

        if (response.getStatusCode() == HttpStatus.OK) {
            LoginResponse loginResponse = parseTokenResponse(response.getBody());

            // Tìm session dựa trên refresh token hiện tại
            Optional<UserSession> sessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
            if (sessionOpt.isPresent()) {
                UserSession session = sessionOpt.get();
                int currentCount = session.getRefreshCount();
                if (currentCount >= MAX_REFRESH_COUNT) {
                    // Nếu vượt quá giới hạn, thu hồi token và xóa session, buộc đăng nhập lại
                    revokeToken(refreshToken);
                    userSessionRepository.delete(session);
                    throw new CustomKeycloakException("Số lần làm mới token đã vượt quá giới hạn. Vui lòng đăng nhập lại.");
                } else {
                    // Tăng số lần refresh, cập nhật access token và refresh token mới
                    session.setRefreshCount(currentCount + 1);
                    session.setToken(loginResponse.getAccessToken());
                    session.setRefreshToken(loginResponse.getRefreshToken());
                    session.setCreatedAt(Instant.now());
                    userSessionRepository.save(session);
                }
            } else {
                System.out.println("Không tìm thấy session cho refresh token: " + refreshToken);
            }
            return loginResponse;
        } else {
            throw new CustomKeycloakException("Error refreshing token: " + response.getStatusCode() + " " + response.getBody());
        }

    }

    public ObjectResponse<String> registerUser(User userInfo) {

        String username = userInfo.getEmail();  // dùng email làm username (có thể điều chỉnh)
        String password = userInfo.getPassword();
        String email = userInfo.getEmail();  // email

        System.out.println("Registering " + username + " with email " + email + " and password " + password);
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            return new ObjectResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to get access token", Instant.now());
        }
        System.out.println("Access token: " + accessToken);
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";
        String userJson = String.format("{\"username\": \"%s\",\"enabled\": true,\"email\": \"%s\",\"firstName\": \"%s\",\"lastName\": \"\",\"credentials\": [{\"type\": \"password\", \"value\": \"%s\", \"temporary\": false}]}", username, email, userInfo.getName(), password);
        ResponseEntity<String> response = sendPostRequestWithAuth(url, userJson, accessToken);
        String userId = response.getHeaders().getLocation().toString();
        userId = userId.substring(userId.lastIndexOf("/") + 1);
        userInfo.setUserId(userId);
        userInfo.setPassword(encoder.encode(password));
        System.out.println(userInfo.getPassword());
        if (response.getStatusCode() == HttpStatus.CREATED) {
            userRepository.save(userInfo);
            return new ObjectResponse<>(HttpStatus.CREATED.value(), "User created successfully", Instant.now());
        } else {
            return new ObjectResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create user: " + response.getBody(), Instant.now());
        }
    }

    private String getAdminAccessToken() {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        String requestBody = String.format("client_id=%s&username=%s&password=%s&grant_type=password", clientId, username, password);
        System.out.println("Request body: " + requestBody);
        System.out.println("URL: " + url);
        ResponseEntity<String> response = sendPostRequest(url, requestBody);
        if (response.getStatusCode() == HttpStatus.OK) {
            return parseTokenResponse(response.getBody()).getAccessToken();
        } else {
            return null;
        }
    }

    //////////////// Các phương thức hỗ trợ mới (THÊM) //////////////////////

    // Tìm userId từ username (dựa vào Admin API của Keycloak)
    public String findUserIdByUsername(String username) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users?username=" + username;
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new CustomKeycloakException("Failed to get admin access token");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            System.out.println("jsonNode: " + jsonNode);
            if (jsonNode.isArray() && jsonNode.size() > 0) {
                return jsonNode.get(0).path("id").asText();
            } else {
                throw new CustomKeycloakException("User not found in Keycloak");
            }
        } catch (JsonProcessingException e) {
            throw new CustomKeycloakException("Error parsing Keycloak response", e);
        }
    }

//    // Thay đổi mật khẩu của người dùng
//    public String changeUserPassword(String username, String newPassword) {
//        String userId = findUserIdByUsername(username);
//        Optional<User> userFind = userRepository.findByEmail(username);
//        if (userFind.isEmpty()) {
//            return "User not found";
//        }
//        User user = userFind.get();
//        // Sử dụng token hợp lệ từ TokenManager
//        String accessToken = tokenManager.getValidAccessToken();
//        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
//        String requestBody = String.format("{\"type\": \"password\", \"value\": \"%s\", \"temporary\": false}", newPassword);
//        ResponseEntity<String> response = sendPutRequestWithAuth(url, requestBody, accessToken);
//        user.setPassword(encoder.encode(newPassword));
//        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
//            userRepository.save(user);
//            return "Password changed successfully";
//        } else {
//            return "Failed to change password: " + response.getBody();
//        }
//    }

    // Thay đổi mật khẩu của người dùng
    public String changeUserPassword(String username, String newPassword) {
        String userId = findUserIdByUsername(username);
        Optional<User> userFind = userRepository.findByEmail(username);
        if (userFind.isEmpty()) {
            return "User not found";
        }
        User user = userFind.get();
        System.out.println("userId: " + userId);
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            return "Failed to get access token";
        }
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
        System.out.println("URL: " + url);
        String requestBody = String.format("{\"type\": \"password\", \"value\": \"%s\", \"temporary\": false}", newPassword);
        ResponseEntity<String> response = sendPutRequestWithAuth(url, requestBody, accessToken);
        System.out.println(response.getStatusCode());
        user.setPassword(encoder.encode(newPassword));
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            userRepository.save(user);
            return "Password changed successfully";
        } else {
            return "Failed to change password: " + response.getBody();
        }
    }


    // Xóa người dùng theo username
    public String deleteUser(String username) {
        String userId = findUserIdByUsername(username);
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            return "Failed to get access token";
        }
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;
        ResponseEntity<String> response = sendDeleteRequestWithAuth(url, accessToken);
        System.out.println("response code: " + response.getStatusCode());
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            return "User deleted successfully";
        } else {
            return "Failed to delete user: " + response.getBody();
        }
    }

    //////////////// Các helper methods (THÊM) //////////////////////

    private ResponseEntity<String> sendPostRequest(String url, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        System.out.println("entity: " + entity);
        return new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
    }

    private ResponseEntity<String> sendPostRequestWithAuth(String url, String requestBody, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        System.out.println("entity: " + entity);
        return new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
    }

    // Helper: gửi PUT request có xác thực
    private ResponseEntity<String> sendPutRequestWithAuth(String url, String requestBody, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        return new RestTemplate().exchange(url, HttpMethod.PUT, entity, String.class);
    }

    // Helper: gửi DELETE request có xác thực
    private ResponseEntity<String> sendDeleteRequestWithAuth(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return new RestTemplate().exchange(url, HttpMethod.DELETE, entity, String.class);
    }

    public LoginResponse getTokenFromAdmin() {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        String requestBody = String.format("client_id=%s&username=%s&password=%s&grant_type=password", clientId, username, password);
        ResponseEntity<String> response = sendPostRequest(url, requestBody);
        if(response.getStatusCode() == HttpStatus.OK){
            return parseTokenResponse(response.getBody());
        } else {
            throw new CustomKeycloakException("Failed to get admin token: " + response.getBody());
        }
    }

    public void revokeToken(String refreshToken) {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
        String requestBody = String.format("client_id=%s&refresh_token=%s", clientId, refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            System.out.println("Failed to revoke token: " + response.getBody());
        } else {
            System.out.println("Successfully revoked token.");
        }
    }


    private LoginResponse parseTokenResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String accessToken = jsonNode.path("access_token").asText();
            String refreshToken = jsonNode.path("refresh_token").asText();
            String tokenType = jsonNode.path("token_type").asText();
            int expiresIn = jsonNode.path("expires_in").asInt();
            // Giữ nguyên cấu trúc LoginResponse theo yêu cầu của bạn
            System.out.println("accessToken: " + accessToken);
            return new LoginResponse(accessToken, refreshToken, tokenType, expiresIn);
        } catch (JsonProcessingException e) {
            throw new CustomKeycloakException("Error parsing the Keycloak response", e);
        }
    }
}
