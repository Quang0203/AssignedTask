package cviettel.loginservice.configuration.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cviettel.loginservice.configuration.keycloack.exception.CustomKeycloakException;
import cviettel.loginservice.configuration.message.Labels;
import cviettel.loginservice.dto.response.common.ObjectResponse;
import cviettel.loginservice.entity.UserSession;
import cviettel.loginservice.enums.MessageCode;
import cviettel.loginservice.exception.handler.UnauthorizedException;
import cviettel.loginservice.repository.UserSessionRepository;
import cviettel.loginservice.service.CustomUserDetailsService;
import cviettel.loginservice.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Retrieve the Authorization header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // Check if the header starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Extract token
            try {
//                username = jwtService.extractUsername(token); // Extract email from token
                username = jwtService.extractEmail(token); // Extract email from token
                System.out.println("Username: " + username);
            } catch (ExpiredJwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json; charset=UTF-8");

                // Tùy thuộc vào cấu trúc ObjectResponse của bạn
                ObjectResponse<String, String> errorResponse = new ObjectResponse<>(
                        MessageCode.MSG1009.name(),  // hoặc ex.getParameters().get("errorCode")
                        Labels.getLabels(MessageCode.MSG1009.getKey()),
                        Instant.now().toString()
                );

                // Chuyển ObjectResponse thành JSON
                String json = new ObjectMapper().writeValueAsString(errorResponse);
                response.getWriter().write(json);
                return;
            }
        }

        // Gọi API introspect của Keycloak để kiểm tra token có còn active không
        if (token != null && !isTokenActive(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");

            // Tùy thuộc vào cấu trúc ObjectResponse của bạn
            ObjectResponse<String, String> errorResponse = new ObjectResponse<>(
                    MessageCode.MSG1008.name(),  // hoặc ex.getParameters().get("errorCode")
                    Labels.getLabels(MessageCode.MSG1008.getKey()),
                    Instant.now().toString()
            );

            // Chuyển ObjectResponse thành JSON
            String json = new ObjectMapper().writeValueAsString(errorResponse);
            response.getWriter().write(json);
            return;
        }

        // If the token is valid and no authentication is set in the context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println("Debug: " + userDetails);

            // Validate token and set authentication
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                System.out.println("authToken: " + authToken);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }

    private boolean isTokenActive(String token) {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";

        // Nếu client là confidential, cần gửi cả client_id và client_secret
        String requestBody = String.format(
                "client_id=%s&client_secret=%s&token=%s",
                "keycloak-spring-task",
                "vU3t9HPvIAkEbtAFttzendISRe9zPwPv",  // lưu clientSecret trong cấu hình của bạn
                token
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = new RestTemplate().exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.path("active").asBoolean();
            } catch (Exception e) {
                throw new CustomKeycloakException(MessageCode.MSG1012);
            }
        }
        return false;
    }

}
