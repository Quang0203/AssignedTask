package cviettel.loginservice.controller;

import cviettel.loginservice.configuration.keycloack.KeycloakUserService;
import cviettel.loginservice.dto.request.ChangePasswordRequest;
import cviettel.loginservice.dto.request.DeleteUserRequest;
import cviettel.loginservice.dto.request.LoginRequest;
import cviettel.loginservice.dto.request.RegisterRequest;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.entity.User;
import cviettel.loginservice.service.AuthService;
import cviettel.loginservice.service.UserService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor // use @RequiredArgsConstructor instead of @Autowired and add "final" to the field
//@NoArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final KeycloakUserService keycloakUserService;

    @PostMapping("/new-user")
    public ObjectResponse<String, Instant> addNewUser(@RequestBody RegisterRequest userRegister) {
        // Đăng ký người dùng trên Keycloak
        return keycloakUserService.registerUser(userRegister);
    }

    @PostMapping("/login")
    public ObjectResponse<LoginResponse, Instant> login(@RequestBody LoginRequest loginRequest) {
        log.info("LoginRequest: {}", loginRequest.getEmail());
        return this.authService.login(loginRequest);
    }

    @PostMapping("/refresh-token")
    public ObjectResponse<LoginResponse, Instant> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        return this.authService.refreshToken(refreshToken);
    }

    // Endpoint thay đổi mật khẩu
    @PostMapping("/change-password")
    @PreAuthorize("hasAuthority('USER')")
    public ObjectResponse<String, Instant> changePassword(@RequestBody ChangePasswordRequest request) {
        String result = keycloakUserService.changeUserPassword(request);
        return new ObjectResponse<>(HttpStatus.OK.value()+"", result, Instant.now());
    }

    // Endpoint xóa người dùng
    @DeleteMapping("/delete-user")
    public ObjectResponse<String, Instant> deleteUser(@RequestBody DeleteUserRequest request) {
        String result = keycloakUserService.deleteUser(request.getUsername());
        return new ObjectResponse<>(HttpStatus.OK.value()+"", result, Instant.now());
    }

    /**
     * Endpoint để lưu token vào Redis.
     * FE sẽ gọi endpoint này sau khi nhận được token từ Keycloak.
     */
    @PostMapping("/save-token")
    public ResponseEntity<String> saveToken(@RequestBody LoginResponse loginResponse) {
        keycloakUserService.saveToken(loginResponse);
        return ResponseEntity.ok("Token saved successfully");
    }
}
