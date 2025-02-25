package cviettel.loginservice.configuration.keycloack;

import cviettel.loginservice.configuration.jwt.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Nếu dùng Spring Boot 3.x, nếu không dùng @EnableGlobalMethodSecurity cho Spring Boot 2.x
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

    private final JwtAuthFilter jwtAuthFilter;

    public KeycloakConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF cho API stateless
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/welcome", "/login", "/new-user").permitAll()
                                .requestMatchers("/login", "/new-user", "/refresh-token", "/delete-user").permitAll()
                                .requestMatchers("/users/user-profile").hasAuthority("USER")
                                .requestMatchers("/admins/**").hasAuthority("ADMIN")
                                .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Mã hóa mật khẩu
    }
}
