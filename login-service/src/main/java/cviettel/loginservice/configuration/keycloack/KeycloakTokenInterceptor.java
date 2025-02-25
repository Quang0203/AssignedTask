//package cviettel.loginservice.configuration.keycloack;
//
//import org.springframework.http.HttpRequest;
//import org.springframework.http.client.ClientHttpRequestExecution;
//import org.springframework.http.client.ClientHttpRequestInterceptor;
//import org.springframework.http.client.ClientHttpResponse;
//
//import java.io.IOException;
//
//public class KeycloakTokenInterceptor implements ClientHttpRequestInterceptor {
//
//    private final TokenManager tokenManager;
//
//    public KeycloakTokenInterceptor(TokenManager tokenManager) {
//        this.tokenManager = tokenManager;
//    }
//
//    @Override
//    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
//        // Lấy access token hợp lệ từ TokenManager
//        String validToken = tokenManager.getValidAccessToken();
//        request.getHeaders().set("Authorization", "Bearer " + validToken);
//        return execution.execute(request, body);
//    }
//}
