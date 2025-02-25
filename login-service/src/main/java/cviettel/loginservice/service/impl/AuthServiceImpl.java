package cviettel.loginservice.service.impl;

import cviettel.loginservice.configuration.keycloack.KeycloakUserService;
import cviettel.loginservice.dto.request.LoginRequest;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
//@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private KeycloakUserService keycloakUserService;

    @Override
    public ObjectResponse<LoginResponse> login(LoginRequest loginRequest) {
        try {
            // Gọi KeycloakUserService để lấy token từ Keycloak
            LoginResponse tokenResponse = keycloakUserService.getToken(loginRequest.getEmail(), loginRequest.getPassword());

            System.out.println("TokenResponse: " + tokenResponse);

            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                System.out.println("Token: " + tokenResponse.getAccessToken());
                return new ObjectResponse<>(HttpStatus.OK.value(), "Login successful", Instant.now(), tokenResponse);
            } else {
                return new ObjectResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid credentials", Instant.now());
            }
        } catch (Exception ex) {
            return new ObjectResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error: " + ex.getMessage(), Instant.now());
        }
    }

    @Override
    public ObjectResponse<LoginResponse> refreshToken(String refreshToken) {
        try {
            LoginResponse tokenResponse = keycloakUserService.refreshToken(refreshToken);
            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                return new ObjectResponse<>(HttpStatus.OK.value(), "Token refreshed successfully", Instant.now(), tokenResponse);
            } else {
                return new ObjectResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid refresh token", Instant.now());
            }
        } catch (Exception ex) {
            return new ObjectResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error refreshing token: " + ex.getMessage(), Instant.now());
        }
    }
}
