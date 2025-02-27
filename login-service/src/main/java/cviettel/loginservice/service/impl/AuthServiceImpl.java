package cviettel.loginservice.service.impl;

import cviettel.loginservice.configuration.keycloack.KeycloakUserService;
import cviettel.loginservice.configuration.message.Labels;
import cviettel.loginservice.dto.request.LoginRequest;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.enums.MessageCode;
import cviettel.loginservice.exception.handler.BadRequestAlertException;
import cviettel.loginservice.exception.handler.InternalServerErrorException;
import cviettel.loginservice.exception.handler.UnauthorizedException;
import cviettel.loginservice.service.AuthService;
import org.apache.coyote.BadRequestException;
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
    public ObjectResponse<LoginResponse, Instant> login(LoginRequest loginRequest) {
        try {
            // Gọi KeycloakUserService để lấy token từ Keycloak
            LoginResponse tokenResponse = keycloakUserService.getToken(loginRequest.getEmail(), loginRequest.getPassword());

            System.out.println("TokenResponse: " + tokenResponse);

            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                System.out.println("Token: " + tokenResponse.getAccessToken());
                return new ObjectResponse<>(HttpStatus.OK.toString(), "Login successful", Instant.now(), tokenResponse);
            } else {
                throw new BadRequestAlertException(MessageCode.MSG1004);
            }
        } catch (Exception ex) {
            throw new UnauthorizedException(MessageCode.MSG1005);
        }
    }

    @Override
    public ObjectResponse<LoginResponse, Instant> refreshToken(String refreshToken) {
        try {
            LoginResponse tokenResponse = keycloakUserService.refreshToken(refreshToken);
            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                return new ObjectResponse<>(HttpStatus.OK.value()+"", "Token refreshed successfully", Instant.now(), tokenResponse);
            } else {
                throw new BadRequestAlertException(MessageCode.MSG1006);
            }
        } catch (Exception ex) {
            throw new InternalServerErrorException(MessageCode.MSG1003);
        }
    }
}
