package com.clouderp.config;

import com.clouderp.exception.BadRequestException;
import com.clouderp.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static UserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentCompanyId() {
        UserPrincipal principal = getCurrentUserPrincipal();
        if (principal != null) {
            return principal.getCompanyId();
        }
        // Fallback for development/testing if no authentication context exists
        return 1L;
    }

    public static Long requireCurrentCompanyId() {
        UserPrincipal principal = getCurrentUserPrincipal();
        if (principal == null) {
            throw new BadRequestException("Authentication required: No authenticated user session found");
        }
        return principal.getCompanyId();
    }

    public static void requireCompanyAdmin() {
        UserPrincipal principal = getCurrentUserPrincipal();
        if (principal == null) {
            throw new ForbiddenException("Authentication required");
        }
        if (!"COMPANY_ADMIN".equalsIgnoreCase(principal.getRole())) {
            throw new ForbiddenException("Access denied: COMPANY_ADMIN role required");
        }
    }

    public static boolean isCompanyAdmin() {
        UserPrincipal principal = getCurrentUserPrincipal();
        return principal != null && "COMPANY_ADMIN".equalsIgnoreCase(principal.getRole());
    }

    public static void requireAnyRole(String... allowedRoles) {
        UserPrincipal principal = getCurrentUserPrincipal();
        if (principal == null) {
            throw new ForbiddenException("Authentication required");
        }
        for (String role : allowedRoles) {
            if (role.equalsIgnoreCase(principal.getRole())) {
                return;
            }
        }
        throw new ForbiddenException("Access denied: Insufficient role permissions");
    }
}
