package cviettel.loginservice.controller;

import cviettel.loginservice.dto.request.LoginRequest;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.entity.User;
import cviettel.loginservice.service.AuthService;
import cviettel.loginservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor // use @RequiredArgsConstructor instead of @Autowired and add "final" to the field
public class AuthController {

    private final UserService userService;

    private final AuthService authService;

    @PostMapping("/new-user")
    public String addNewUser(@RequestBody User userInfo) {
        return userService.addUser(userInfo);
    }

    @GetMapping("/users/user-profile") // have to user plural noun for the endpoint
    @PreAuthorize("hasAuthority('USER')") // use "-" instead of camelCase
    public ObjectResponse<String> userProfile() {
        return new ObjectResponse<>(HttpStatus.OK.value(), "Welcome to User Profile", Instant.now());
    }

    @GetMapping("/admins/admin-profile")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminProfile() {
        return "Welcome to Admin Profile";
    }

    @PostMapping("/login")
    public ObjectResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return this.authService.login(loginRequest);
    }
}
