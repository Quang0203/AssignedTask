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
        return "User created successfully in both local DB and Keycloak.";
    }

}