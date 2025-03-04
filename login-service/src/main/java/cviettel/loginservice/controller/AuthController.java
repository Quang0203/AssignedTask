package cviettel.loginservice.controller;

import cviettel.loginservice.configuration.keycloack.KeycloakUserService;
import cviettel.loginservice.dto.request.ChangePasswordRequest;
import cviettel.loginservice.dto.request.DeleteUserRequest;
import cviettel.loginservice.dto.request.LoginRequest;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.entity.User;
import cviettel.loginservice.service.AuthService;
import cviettel.loginservice.service.UserService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
//@RequiredArgsConstructor // use @RequiredArgsConstructor instead of @Autowired and add "final" to the field
//@NoArgsConstructor
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private KeycloakUserService keycloakUserService;

    @PostMapping("/new-user")
    public ObjectResponse<String, Instant> addNewUser(@RequestBody User userInfo) {

        // Đăng ký người dùng trên Keycloak
        return keycloakUserService.registerUser(userInfo);
    }

    @GetMapping("/users/user-profile") // have to user plural noun for the endpoint
    @PreAuthorize("hasAuthority('USER')") // use "-" instead of camelCase
    public ObjectResponse<String, Instant> userProfile() {
        return new ObjectResponse<>(HttpStatus.OK.value()+"", "Welcome to User Profile", Instant.now());
    }

    @GetMapping("/admins/admin-profile")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ObjectResponse<String, Instant> adminProfile() {
        return new ObjectResponse<>(HttpStatus.OK.value()+"", "Welcome to Admin Profile", Instant.now());
    }

    @PostMapping("/login")
    public ObjectResponse<LoginResponse, Instant> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("LoginRequest: " + loginRequest.getEmail());
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
        String result = keycloakUserService.changeUserPassword(request.getUsername(), request.getNewPassword());
        return new ObjectResponse<>(HttpStatus.OK.value()+"", result, Instant.now());
    }

    // Endpoint xóa người dùng
    @DeleteMapping("/delete-user")
    public ObjectResponse<String, Instant> deleteUser(@RequestBody DeleteUserRequest request) {
        String result = keycloakUserService.deleteUser(request.getUsername());
        return new ObjectResponse<>(HttpStatus.OK.value()+"", result, Instant.now());
    }
}
