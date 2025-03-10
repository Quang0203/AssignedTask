package cviettel.loginservice.configuration.auditing;

import cviettel.loginservice.util.ThreadLocalUtil;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareConfig implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        } else if (!authentication.getName().equals("anonymousUser")) {
            return Optional.of(authentication.getName());
        }else {
            return Optional.of("USER_SIGN_UP");
        }
    }
}

