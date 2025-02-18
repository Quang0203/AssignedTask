package cviettel.orderservice.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Vì không có DB user, tạo đối tượng UserDetails đơn giản với username đã lấy từ token
        return new User(username, "", Collections.emptyList());
    }
}