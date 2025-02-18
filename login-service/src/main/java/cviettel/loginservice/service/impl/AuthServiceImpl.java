package cviettel.loginservice.service.impl;

import cviettel.loginservice.dto.request.LoginRequest;
import cviettel.loginservice.dto.response.LoginResponse;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.entity.User;
import cviettel.loginservice.entity.UserSession;
import cviettel.loginservice.repository.UserRepository;
import cviettel.loginservice.repository.UserSessionRepository;
import cviettel.loginservice.service.AuthService;
import cviettel.loginservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final UserSessionRepository userSessionRepository;

    private final JwtService jwtUtil;

    @Override
    public ObjectResponse<LoginResponse> login(LoginRequest loginRequest) {
        try {
            // Xác thực username và password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            System.out.println("Check 1");

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> userFind = userRepository.findByEmail(loginRequest.getEmail());
            User user = userFind.get();
            System.out.println("User: " + user.getEmail());

            // Kiểm tra phiên đăng nhập hiện tại (chỉ cho phép 1 thiết bị)
            Optional<UserSession> existingSession = userSessionRepository.findByUserId(user.getUserId());
            if (existingSession.isPresent()) {
                // Xoá phiên đăng nhập cũ (hoặc bạn có thể từ chối đăng nhập mới)
                userSessionRepository.delete(existingSession.get());
            }

            // Tạo JWT mới với thời hạn 30 phút
            String token = jwtUtil.generateToken(user.getEmail());

            // Lưu thông tin phiên đăng nhập mới
            UserSession session = new UserSession();
            session.setUserId(user.getUserId());
            session.setToken(token);
            session.setCreatedAt(Instant.now());
            userSessionRepository.save(session);

            return new ObjectResponse<>(HttpStatus.OK.value(), "Successful Login", Instant.now(), new LoginResponse(token));
        } catch (BadCredentialsException ex) {
            return new ObjectResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid username or password", Instant.now());
        }
    }
}
