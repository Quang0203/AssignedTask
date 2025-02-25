//package cviettel.loginservice.configuration.keycloack;
//
//import cviettel.loginservice.configuration.keycloack.exception.CustomKeycloakException;
//import cviettel.loginservice.dto.response.LoginResponse;
//import cviettel.loginservice.service.JwtService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
//@Component
//public class TokenManager {
//
//    private String accessToken;
//    private String refreshToken;
//
//    @Autowired
//    private KeycloakUserService keycloakUserService;
//
//    @Autowired
//    private JwtService jwtService;
//
//    /**
//     * Lấy access token hợp lệ.
//     * Nếu token chưa có hoặc đã hết hạn, tự động làm mới.
//     */
//    public synchronized String getValidAccessToken() {
//        if (accessToken == null || isTokenExpired()) {
//            refreshOrObtainNewToken();
//        }
//        return accessToken;
//    }
//
//    private boolean isTokenExpired() {
//        try {
//            // Sử dụng JwtService để lấy thời gian hết hạn từ token
//            Date expiration = jwtService.extractAllClaims(accessToken).getExpiration();
//            return expiration.before(new Date());
//        } catch (Exception e) {
//            return true;
//        }
//    }
//
//    private void refreshOrObtainNewToken() {
//        LoginResponse tokenResponse;
//        if (refreshToken != null) {
//            try {
//                // Thử dùng refresh token để lấy access token mới
//                tokenResponse = keycloakUserService.refreshToken(refreshToken);
//            } catch (Exception ex) {
//                // Nếu refresh thất bại, đăng nhập lại bằng thông tin admin
//                tokenResponse = keycloakUserService.getTokenFromAdmin();
//            }
//        } else {
//            tokenResponse = keycloakUserService.getTokenFromAdmin();
//        }
//        updateToken(tokenResponse);
//    }
//
//    private void updateToken(LoginResponse tokenResponse) {
//        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
//            throw new CustomKeycloakException("Không lấy được token mới từ Keycloak");
//        }
//        this.accessToken = tokenResponse.getAccessToken();
//        this.refreshToken = tokenResponse.getRefreshToken();
//    }
//}
