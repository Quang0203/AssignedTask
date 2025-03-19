package cviettel.loginservice.service.impl;

import cviettel.loginservice.configuration.keycloack.KeycloakUserService;
import cviettel.loginservice.dto.request.AdminProfileRequest;
import cviettel.loginservice.dto.request.UserProfileRequest;
import cviettel.loginservice.dto.response.AdminProfileResponse;
import cviettel.loginservice.dto.response.UserProfileResponse;
import cviettel.loginservice.entity.User;
import cviettel.loginservice.repository.UserRepository;
import cviettel.loginservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public UserProfileResponse getUserProfile(UserProfileRequest userProfileRequest) {
        // Lấy thông tin người dùng từ DB nội bộ
        User user = repository.findById(userProfileRequest.getId()).orElse(null);
        if (user == null) {
            return null;
        }
        return UserProfileResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    @Override
    public AdminProfileResponse getAdminProfile(AdminProfileRequest adminProfileRequest) {
        User user = repository.findById(adminProfileRequest.getId()).orElse(null);
        if (user == null) {
            return null;
        }
        return AdminProfileResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

}