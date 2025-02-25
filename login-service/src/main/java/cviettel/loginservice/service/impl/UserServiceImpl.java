package cviettel.loginservice.service.impl;

import cviettel.loginservice.configuration.keycloack.KeycloakUserService;
import cviettel.loginservice.entity.User;
import cviettel.loginservice.repository.UserRepository;
import cviettel.loginservice.service.UserService;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder encoder;

//    @Autowired
//    private KeycloakUserService keycloakUserService;

    @Override
    public String addUser(User userInfo) {
        // Lưu người dùng vào DB nội bộ
        repository.save(userInfo);

//        // Gọi API đăng ký người dùng trên Keycloak
//        String keycloakResult = keycloakUserService.registerUser(
//                userInfo.getEmail(),      // Sử dụng email làm username (có thể điều chỉnh nếu cần)
//                userInfo.getPassword(),   // Mật khẩu người dùng
//                userInfo.getEmail()       // Email của người dùng
//        );
//
//        // Kiểm tra kết quả trả về từ Keycloak
//        if (keycloakResult.contains("Failed")) {
//            // Tùy chọn: Nếu muốn rollback DB khi Keycloak thất bại, cần triển khai transaction management
//            return "User created locally, but error when registering in Keycloak: " + keycloakResult;
//        }
        return "User created successfully in both local DB and Keycloak.";
    }

//    public String addUser(User userInfo) {
//        // Encode password before saving the user
//        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
//        repository.save(userInfo);
//        return "User Added Successfully";
//    }
}