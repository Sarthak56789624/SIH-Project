package com.aichainid.security;

import com.aichainid.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentUserDetails().getEmail();
    }

    public static List<String> getCurrentUserRoles() {
        return getCurrentUserDetails().getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
    }

    public static boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getCurrentUserRoles().contains(roleWithPrefix);
    }

    public static boolean hasPermission(String permission) {
        return getCurrentUserRoles().contains(permission);
    }

    public static String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
                return request.getRemoteAddr();
            }
            return xfHeader.split(",")[0].trim();
        }
        return "127.0.0.1";
    }

    public static String getClientUserAgent() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userAgent = request.getHeader("User-Agent");
            return userAgent != null ? userAgent : "Unknown";
        }
        return "Unknown";
    }
}
