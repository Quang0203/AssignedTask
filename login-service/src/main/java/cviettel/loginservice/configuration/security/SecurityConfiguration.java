//package cviettel.loginservice.configuration.security;
//
//import cviettel.loginservice.configuration.jwt.JwtAuthFilter;
//import cviettel.loginservice.service.CustomUserDetailsService;
//import org.keycloak.adapters.KeycloakConfigResolver;
//import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class SecurityConfiguration {
//
//    @Autowired
//    private JwtAuthFilter authFilter;
//
//    // Bean KeycloakConfigResolver cho Keycloak adapter
//    @Bean
//    public KeycloakConfigResolver keycloakConfigResolver() {
//        return new KeycloakSpringBootConfigResolver();
//    }
//
//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new CustomUserDetailsService(); // CustomUserDetailsService implements UserDetailsService
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // Tắt CSRF cho API stateless
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/welcome", "/login", "/new-user").permitAll()
//                        .requestMatchers("/users/**").hasAuthority("USER")
//                        .requestMatchers("/admins/**").hasAuthority("ADMIN")
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(sess -> sess
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .authenticationProvider(authenticationProvider())
//                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(); // Mã hóa mật khẩu
//    }
//
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userDetailsService());
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//}
