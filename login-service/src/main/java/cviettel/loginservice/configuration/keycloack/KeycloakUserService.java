package cviettel.loginservice.configuration.keycloack;

import cviettel.loginservice.configuration.keycloack.exception.CustomKeycloakException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cviettel.loginservice.configuration.message.LabelKey;
import cviettel.loginservice.configuration.message.Labels;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.entity.User;
import cviettel.loginservice.enums.MessageCode;
import cviettel.loginservice.exception.handler.InternalServerErrorException;
import cviettel.loginservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
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

    @Value("${keycloak.credentials.secret}")
    private String secretKey;

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public LoginResponse getToken(String username, String password) {
        // Bước 1: Lấy userId của user từ Keycloak
        String userId = findUserIdByUsername(username);
        if (userId != null) {
            // Bước 2: Kiểm tra xem user đã có phiên đăng nhập chưa
            if (hasActiveSession(userId)) {
                // Nếu có phiên cũ, gọi API để logout tất cả phiên trước đó
                logoutUserSessions(userId);
            }
        } else {
            throw new InternalServerErrorException(MessageCode.MSG1000);
        }

        // Bước 3: Gọi API token của Keycloak với grant_type=password
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        String requestBody = String.format("client_id=%s&username=%s&password=%s&grant_type=password&client_secret=%s", clientId, username, password, secretKey);
        ResponseEntity<String> response = sendPostRequest(url, requestBody);
        System.out.println("Response at getToken(): " + response);

        if (response.getStatusCode() == HttpStatus.OK) {
            LoginResponse loginResponse = parseTokenResponse(response.getBody());
            redisTemplate.opsForValue().set("TokenLogin", loginResponse.getAccessToken(), loginResponse.getExpiresIn(), TimeUnit.SECONDS);
            return loginResponse;
        } else {
            throw new CustomKeycloakException(Labels.getLabels(MessageCode.MSG1010.getKey()) + response.getStatusCode() + " " + response.getBody(), MessageCode.MSG1010.name(), MessageCode.MSG1010.getKey());
        }
    }


    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        // 1) Check if the refresh token is still active via Keycloak introspect
        if (!isTokenActive(refreshToken)) {
            throw new CustomKeycloakException(MessageCode.MSG1011);
        }

        // 2) If active, call the Keycloak token endpoint with grant_type=refresh_token
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        String requestBody = String.format(
                "client_id=%s&refresh_token=%s&grant_type=refresh_token",
                clientId,
                refreshToken
        );
        ResponseEntity<String> response = sendPostRequest(url, requestBody);

        if (response.getStatusCode() == HttpStatus.OK) {
            // 3) Parse response and build LoginResponse
            LoginResponse loginResponse = parseTokenResponse(response.getBody());
            // At this point, the old refresh token is invalidated if “Refresh Token Max Reuse = 0”.
            return loginResponse;
        } else {
            throw new CustomKeycloakException(
                    Labels.getLabels(MessageCode.MSG1003.getKey()) + response.getStatusCode() + " " + response.getBody(),
                    MessageCode.MSG1003.name(),
                    MessageCode.MSG1003.getKey()
            );
        }
    }

    private boolean isTokenActive(String token) {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";

        // Nếu client là confidential, cần gửi cả client_id và client_secret
        String requestBody = String.format(
                "client_id=%s&client_secret=%s&token=%s",
                "keycloak-spring-task",
                "vU3t9HPvIAkEbtAFttzendISRe9zPwPv",  // lưu clientSecret trong cấu hình của bạn
                token
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.path("active").asBoolean();
            } catch (Exception e) {
                throw new CustomKeycloakException(MessageCode.MSG1012);
            }
        }
        return false;
    }


    public ObjectResponse<String, Instant> registerUser(User userInfo) {
        System.out.println("Registering " + userInfo.getEmail() + " with email " + userInfo.getEmail() + " and password " + userInfo.getPassword());
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new InternalServerErrorException(MessageCode.MSG1013);
        }
        System.out.println("Access token: " + accessToken);
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";
        String userJson = String.format("{\"username\": \"%s\",\"enabled\": true,\"email\": \"%s\",\"firstName\": \"%s\",\"lastName\": \"\",\"credentials\": [{\"type\": \"password\", \"value\": \"%s\", \"temporary\": false}]}", username, userInfo.getEmail(), userInfo.getName(), password);
        ResponseEntity<String> response = sendPostRequestWithAuth(url, userJson, accessToken);
        String userId = response.getHeaders().getLocation().toString();
        userId = userId.substring(userId.lastIndexOf("/") + 1);
        userInfo.setUserId(userId);
        userInfo.setPassword(encoder.encode(password));
        System.out.println(userInfo.getPassword());
        if (response.getStatusCode() == HttpStatus.CREATED) {
            userRepository.save(userInfo);
            return new ObjectResponse<>(HttpStatus.OK.toString(), "User created successfully", Instant.now());
        } else {
            return new ObjectResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value() + "", "Failed to create user: " + response.getBody(), Instant.now());
        }
    }

    private String getAdminAccessToken() {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        String requestBody = String.format("client_id=%s&username=%s&password=%s&grant_type=password&client_secret=%s", clientId, username, password, secretKey);
        System.out.println("Request body: " + requestBody);
        System.out.println("URL in the getting admin access token stage: " + url);
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
        System.out.println("URL in the finding user id stage: " + url);
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new CustomKeycloakException(MessageCode.MSG1012);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            System.out.println("jsonNode: " + jsonNode);
            if (jsonNode.isArray() && !jsonNode.isEmpty()) {
                return jsonNode.get(0).path("id").asText();
            } else {
                throw new CustomKeycloakException(MessageCode.MSG1014);
            }
        } catch (JsonProcessingException e) {
            throw new CustomKeycloakException(MessageCode.MSG1015);
        }
    }

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

    // Kiểm tra xem user (theo userId) có phiên đăng nhập nào đang hoạt động không
    private boolean hasActiveSession(String userId) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/sessions";
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new CustomKeycloakException(MessageCode.MSG1013);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());
            // Nếu mảng kết quả không rỗng thì có phiên đăng nhập đang hoạt động
            return node.isArray() && node.size() > 0;
        } catch (Exception e) {
            throw new CustomKeycloakException(MessageCode.MSG1016);
        }
    }

    // Logout tất cả phiên đăng nhập của user (theo userId)
    @Transactional
    protected void logoutUserSessions(String userId) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/logout";
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new CustomKeycloakException(MessageCode.MSG1013);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                System.out.println("Failed to logout user sessions: " + response.getBody());
            } else {
                redisTemplate.delete("TokenLogin");
                System.out.println("Successfully logged out previous sessions for userId: " + userId);
            }
        } catch (HttpClientErrorException e) {
            // Nếu nhận được 404, nghĩa là không có phiên nào, nên có thể bỏ qua lỗi này
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                System.out.println("No active sessions found for userId: " + userId + ". Nothing to logout.");
            } else {
                System.out.println("Error during logoutUserSessions: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            }
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
            throw new CustomKeycloakException(MessageCode.MSG1015);
        }
    }
}
