package cviettel.loginservice.configuration.keycloack;

//import cviettel.loginservice.configuration.jwt.TrustedHeaderAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Nếu dùng Spring Boot 3.x, nếu không dùng @EnableGlobalMethodSecurity cho Spring Boot 2.x
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class KeycloakConfig {

//    @Autowired
//    private TrustedHeaderAuthenticationFilter trustedHeaderAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(java.util.List.of("*"));
                    corsConfiguration.setAllowedMethods(java.util.List.of("*"));
                    corsConfiguration.setAllowedHeaders(java.util.List.of("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                }))
//                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF cho API stateless
//                .addFilterBefore(trustedHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Tuỳ bạn có endpoint public nào không
                        .requestMatchers("/login",
                                "/new-user",
                                "/refresh-token",
                                "/delete-user",
                                "/auth/swagger-ui.html",   // Cho phép truy cập file gốc swagger-ui
                                "/auth/swagger-ui/**",     // Cho phép truy cập các resource của swagger-ui
                                "/auth/v3/api-docs/**",     // Cho phép truy cập OpenAPI JSON
                                "/auth/v3/api-docs",     // Cho phép truy cập OpenAPI JSON
                                "/swagger-ui.html",   // Cho phép truy cập file gốc swagger-ui
                                "/swagger-ui/**",     // Cho phép truy cập các resource của swagger-ui
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                                ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
