package cviettel.loginservice.controller;

import cviettel.loginservice.dto.request.AdminProfileRequest;
import cviettel.loginservice.dto.request.UserProfileRequest;
import cviettel.loginservice.dto.response.AdminProfileResponse;
import cviettel.loginservice.dto.response.UserProfileResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/user-profile") // sử dụng danh từ số nhiều cho endpoint
    @PreAuthorize("hasAuthority('USER')")
    public ObjectResponse<UserProfileResponse, Instant> userProfile(@RequestParam("id") String id) {
        // Tạo DTO từ query param
        UserProfileRequest request = UserProfileRequest.builder()
                .id(id)
                .build();
        return new ObjectResponse<>(HttpStatus.OK.value()+"", "Welcome to User Profile", Instant.now(), userService.getUserProfile(request));
    }

    @GetMapping("/admins/admin-profile")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ObjectResponse<AdminProfileResponse, Instant> adminProfile(@RequestParam("id") String id) {
        // Tạo DTO từ query param
        AdminProfileRequest request = AdminProfileRequest.builder()
                .id(id)
                .build();
        return new ObjectResponse<>(HttpStatus.OK.value()+"", "Welcome to Admin Profile", Instant.now(), userService.getAdminProfile(request));
    }
}
