package org.wy.demo.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Integer getCurrentUserId() {
        Authentication authentication = requireAuthentication();
        Object details = authentication.getDetails();
        if (details instanceof Integer) {
            return (Integer) details;
        }
        throw new AccessDeniedException("Authenticated user id is missing");
    }

    public static String getCurrentRole() {
        Authentication authentication = requireAuthentication();
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", "").toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user role is missing"));
    }

    public static boolean hasRole(String role) {
        return role != null && role.equalsIgnoreCase(getCurrentRole());
    }

    public static void requireAnyRole(String... roles) {
        String currentRole = getCurrentRole();
        for (String role : roles) {
            if (role != null && role.equalsIgnoreCase(currentRole)) {
                return;
            }
        }
        throw new AccessDeniedException("You do not have permission to access this resource");
    }

    private static Authentication requireAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }
        return authentication;
    }
}
