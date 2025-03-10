package cviettel.loginservice.service;

import cviettel.loginservice.entity.User;
import cviettel.loginservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    private CustomUserDetails customUserDetails;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userDetail = userRepository.findByEmail(email);
        if(userDetail.isEmpty()){
            throw new UsernameNotFoundException("User not found: " + email);
        }

        return userDetail.map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
