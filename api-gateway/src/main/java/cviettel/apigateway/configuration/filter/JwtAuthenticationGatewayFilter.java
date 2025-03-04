package cviettel.apigateway.configuration.filter;

import cviettel.apigateway.configuration.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtService jwtService; // Sử dụng JwtService của bạn để validate token

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Lấy path
        String path = exchange.getRequest().getURI().getPath();

        // Nếu path là /api/v1/auth/login thì bỏ qua bước kiểm tra token
        if (path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/new-user")
                || path.startsWith("/api/v1/auth/refresh-token")) {
            return chain.filter(exchange);
        }

        // Ngược lại, kiểm tra token
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtService.validateToken(token)) {
                return unauthorizedResponse(exchange, "Token expired");
            }
        } catch (ExpiredJwtException e) {
            return unauthorizedResponse(exchange, "Token expired");
        } catch (Exception e) {
            return unauthorizedResponse(exchange, "Invalid token: " + e.getMessage());
        }

        // Cho phép request đi tiếp nếu token hợp lệ
        return chain.filter(exchange);
    }


    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"error\": \"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        // Ưu tiên cao để xác thực sớm
        return -1;
    }
}
