package org.ngs.payments.util;

import lombok.experimental.UtilityClass;
import org.ngs.payments.exception.InvalidCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtils {
    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long userId)) {
            throw new InvalidCredentialsException("Not authenticated");
        }
        return userId;
    }
}
