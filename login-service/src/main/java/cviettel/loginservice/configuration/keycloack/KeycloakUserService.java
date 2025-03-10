package cviettel.loginservice.configuration.keycloack;

import cviettel.loginservice.configuration.keycloack.exception.CustomKeycloakException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cviettel.loginservice.configuration.message.LabelKey;
import cviettel.loginservice.configuration.message.Labels;
import cviettel.loginservice.dto.request.ChangePasswordRequest;
import cviettel.loginservice.dto.request.RegisterRequest;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.entity.User;
import cviettel.loginservice.enums.MessageCode;
import cviettel.loginservice.exception.handler.BadRequestAlertException;
import cviettel.loginservice.exception.handler.InternalServerErrorException;
import cviettel.loginservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        log.info("Response at getToken(): {}", response);

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

    /**
     * Đăng ký người dùng trên Keycloak và gán role (admin hoặc user)
     */
    public ObjectResponse<String, Instant> registerUser(RegisterRequest userRegister) {
        User userInfo = User.builder()
                .email(userRegister.getEmail())
                .name(userRegister.getName())
                .password(userRegister.getPassword())
                .role(userRegister.getRole())
                .status(userRegister.getStatus())
                .isVerified(userRegister.getIsVerified())
                .build();
        log.info("Registering {} with email {} and password {}", userInfo.getEmail(), userInfo.getEmail(), userInfo.getPassword());
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new InternalServerErrorException(MessageCode.MSG1013);
        }
        log.info("Access token: {}", accessToken);

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";
        String userJson = String.format(
                "{\"username\": \"%s\",\"enabled\": true,\"email\": \"%s\",\"firstName\": \"%s\",\"lastName\": \"\",\"credentials\": [{\"type\": \"password\", \"value\": \"%s\", \"temporary\": false}]}",
                userInfo.getEmail(), userInfo.getEmail(), userInfo.getName(), userInfo.getPassword()
        );
        ResponseEntity<String> response = sendPostRequestWithAuth(url, userJson, accessToken);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            // Lấy userId từ header Location
            String locationHeader = response.getHeaders().getLocation().toString();
            String userId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
            userInfo.setUserId(userId);
            userInfo.setPassword(encoder.encode(userInfo.getPassword()));
            userInfo.setCreatedBy(userId);
            userInfo.setUpdatedBy(userId);
            log.info("Created user with Keycloak userId: {}", userId);

            // Xác định role cần gán: nếu userInfo có role = "admin" thì gán admin, ngược lại gán "user" (mặc định)
            String roleToAssign = "user";
            if (userInfo.getRole() != null && userInfo.getRole().equalsIgnoreCase("admin")) {
                roleToAssign = "admin";
            }
            boolean roleAssigned = assignRealmRoleToUser(accessToken, userId, roleToAssign);
            if (!roleAssigned) {
                throw new InternalServerErrorException(MessageCode.MSG1017);
            }
            // Lưu user vào DB cục bộ
            userRepository.save(userInfo);
            return new ObjectResponse<>(HttpStatus.OK.toString(), "User created successfully with role: " + roleToAssign, Instant.now());
        } else {
            throw new InternalServerErrorException(Labels.getLabels(MessageCode.MSG1018.getKey()) + response.getBody(), MessageCode.MSG1018.name(), MessageCode.MSG1018.getKey());
        }
    }

    /**
     * Lấy Admin Access Token từ Keycloak (sử dụng grant_type = password)
     */
    private String getAdminAccessToken() {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        String requestBody = String.format("client_id=%s&username=%s&password=%s&grant_type=password&client_secret=%s", clientId, username, password, secretKey);
        log.info("Request body: {}", requestBody);
        log.info("URL in the getting admin access token stage: {}", url);
        ResponseEntity<String> response = sendPostRequest(url, requestBody);
        if (response.getStatusCode() == HttpStatus.OK) {
            return parseTokenResponse(response.getBody()).getAccessToken();
        } else {
            return null;
        }
    }

    /**
     * Gán một realm role (admin hoặc user) cho user có userId
     *
     * @param accessToken Access token của admin
     * @param userId      Id của user cần gán role
     * @param roleName    Tên role cần gán
     * @return true nếu gán role thành công, ngược lại false
     *
     */
    private boolean assignRealmRoleToUser(String accessToken, String userId, String roleName) {
        try {
            // Lấy thông tin role từ Keycloak
            String getRoleUrl = keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + roleName;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> roleResponse = restTemplate.exchange(getRoleUrl, HttpMethod.GET, entity, String.class);
            if (roleResponse.getStatusCode() != HttpStatus.OK) {
                log.error("Failed to get role {}: {}", roleName, roleResponse.getStatusCode());
                throw new InternalServerErrorException(Labels.getLabels(MessageCode.MSG1019.getKey()) + roleName + roleResponse.getBody(), MessageCode.MSG1019.name(), MessageCode.MSG1019.getKey());
            }
            JsonNode roleJson = objectMapper.readTree(roleResponse.getBody());
            String roleId = roleJson.get("id").asText();
            String roleRealName = roleJson.get("name").asText();

            // Gán role cho user thông qua API role-mappings
            String roleMappingUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
            ArrayNode rolesArray = objectMapper.createArrayNode();
            ObjectNode roleNode = objectMapper.createObjectNode();
            roleNode.put("id", roleId);
            roleNode.put("name", roleRealName);
            rolesArray.add(roleNode);

            HttpHeaders roleHeaders = new HttpHeaders();
            roleHeaders.setBearerAuth(accessToken);
            roleHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> roleRequest = new HttpEntity<>(rolesArray.toString(), roleHeaders);
            ResponseEntity<String> assignResponse = restTemplate.exchange(roleMappingUrl, HttpMethod.POST, roleRequest, String.class);
            if (assignResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("Role {} assigned to userId {} successfully", roleRealName, userId);
                return true;
            } else {
                log.error("Failed to assign role {} to userId {}: {}", roleRealName, userId, assignResponse.getBody());
                return false;
            }
        } catch (Exception e) {
            throw new InternalServerErrorException(Labels.getLabels(MessageCode.MSG1020.getKey()) + e.getMessage(), MessageCode.MSG1020.name(), MessageCode.MSG1020.getKey());
        }
    }

    //////////////// Các helper methods //////////////////////

    private ResponseEntity<String> sendPostRequest(String url, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        log.info("entity: {}", entity);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    private ResponseEntity<String> sendPostRequestWithAuth(String url, String requestBody, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        log.info("entity: {}", entity);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    private ResponseEntity<String> sendPutRequestWithAuth(String url, String requestBody, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }

    private ResponseEntity<String> sendDeleteRequestWithAuth(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }

    private boolean isTokenActive(String token) {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";
        String requestBody = String.format(
                "client_id=%s&client_secret=%s&token=%s",
                "keycloak-spring-task",
                "vU3t9HPvIAkEbtAFttzendISRe9zPwPv",
                token
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.path("active").asBoolean();
            } catch (Exception e) {
                throw new CustomKeycloakException(MessageCode.MSG1012);
            }
        }
        return false;
    }

    public String findUserIdByUsername(String username) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users?username=" + username;
        log.info("URL in the finding user id stage: {}", url);
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new CustomKeycloakException(MessageCode.MSG1012);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            log.info("jsonNode: {}", jsonNode);
            if (jsonNode.isArray() && jsonNode.size() > 0) {
                return jsonNode.get(0).path("id").asText();
            } else {
                throw new CustomKeycloakException(MessageCode.MSG1014);
            }
        } catch (JsonProcessingException e) {
            throw new CustomKeycloakException(MessageCode.MSG1015);
        }
    }

    /**
     * Change user password in Keycloak
     *
     * @param request ChangePasswordRequest object
     * @return
     */
    public String changeUserPassword(ChangePasswordRequest request) {
        String oldPassword = request.getOldPassword();
        String username = request.getUsername();
        String newPassword = request.getNewPassword();

        if (oldPassword.equals(newPassword)) {
            throw new BadRequestAlertException(MessageCode.MSG1022);
        }

        String userId = findUserIdByUsername(username);
        Optional<User> userFind = userRepository.findByEmail(username);
        if (userFind.isEmpty()) {
            throw new InternalServerErrorException(MessageCode.MSG1000);
        }
        User user = userFind.get();
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestAlertException(MessageCode.MSG1021);
        }

        log.info("userId: {}", userId);
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new CustomKeycloakException(MessageCode.MSG1013);
        }
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
        log.info("URL: {}", url);
        String requestBody = String.format("{\"type\": \"password\", \"value\": \"%s\", \"temporary\": false}", newPassword);
        ResponseEntity<String> response = sendPutRequestWithAuth(url, requestBody, accessToken);
        log.info(response.getStatusCode().toString());
        user.setPassword(encoder.encode(newPassword));
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            userRepository.save(user);
            return "Password changed successfully";
        } else {
            throw new InternalServerErrorException(MessageCode.MSG1023);
        }
    }

    public String deleteUser(String username) {
        String userId = findUserIdByUsername(username);
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new CustomKeycloakException(MessageCode.MSG1013);
        }
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;
        ResponseEntity<String> response = sendDeleteRequestWithAuth(url, accessToken);
        log.info("response code: {}", response.getStatusCode());
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            return "User deleted successfully";
        } else {
            throw new InternalServerErrorException(MessageCode.MSG1024);
        }
    }

    private boolean hasActiveSession(String userId) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/sessions";
        String accessToken = getAdminAccessToken();
        if (accessToken == null) {
            throw new CustomKeycloakException(MessageCode.MSG1013);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        try {
            JsonNode node = objectMapper.readTree(response.getBody());
            return node.isArray() && node.size() > 0;
        } catch (Exception e) {
            throw new CustomKeycloakException(MessageCode.MSG1016);
        }
    }

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
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
                log.info("Failed to logout user sessions: {}", response.getBody());
            } else {
                redisTemplate.delete("TokenLogin");
                log.info("Successfully logged out previous sessions for userId: {}", userId);
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.error("No active sessions found for userId: {}. Nothing to logout.", userId);
            } else {
                log.error("Error during logoutUserSessions: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            }
        }
    }

    private LoginResponse parseTokenResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String accessToken = jsonNode.path("access_token").asText();
            String refreshToken = jsonNode.path("refresh_token").asText();
            String tokenType = jsonNode.path("token_type").asText();
            int expiresIn = jsonNode.path("expires_in").asInt();
            log.info("accessToken: {}", accessToken);
            return new LoginResponse(accessToken, refreshToken, tokenType, expiresIn);
        } catch (JsonProcessingException e) {
            throw new CustomKeycloakException(MessageCode.MSG1015);
        }
    }
}
