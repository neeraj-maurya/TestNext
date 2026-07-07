package com.testnext.security;

import com.testnext.user.SystemUser;
import com.testnext.user.SystemUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Authentication filter supporting multiple credential schemes:
 * 1. X-TestNext-User header — user impersonation (dev/test only, no password required)
 * 2. x-api-key header — API key authentication
 * 3. Authorization: Basic — username/password authentication (BCrypt verified)
 */
@Component
public class DevAuthFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DevAuthFilter.class);

    private final SystemUserRepository userRepo;
    private final com.testnext.repository.TenantRepository tenantRepo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public DevAuthFilter(SystemUserRepository userRepo,
            com.testnext.repository.TenantRepository tenantRepo,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.tenantRepo = tenantRepo;
        this.passwordEncoder = passwordEncoder;
        log.debug("DevAuthFilter: Initialized");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("DevAuthFilter: Processing {} {}", request.getMethod(), request.getRequestURI());

        try {
            // 1. Check for User Switching Header (dev/test impersonation — no password required)
            String switchUser = request.getHeader("X-TestNext-User");
            if (switchUser != null && !switchUser.isBlank()) {
                Authentication auth = authFromUsername(switchUser.trim());
                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            // 2. Check for API Key
            String apiKey = request.getHeader("x-api-key");
            if (apiKey != null && !apiKey.isBlank()) {
                Authentication auth = authFromApiKey(apiKey.trim());
                if (auth != null)
                    SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // 3. Check for Basic Auth
                String authz = request.getHeader("Authorization");
                if (authz != null && authz.startsWith("Basic ")) {
                    String b64 = authz.substring(6).trim();
                    try {
                        String decoded = new String(Base64.getDecoder().decode(b64));
                        int idx = decoded.indexOf(':');
                        if (idx > 0) {
                            String user = decoded.substring(0, idx);
                            String pass = decoded.substring(idx + 1);
                            Authentication auth = authFromBasic(user, pass);
                            if (auth != null)
                                SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            log.error("DevAuthFilter: Authentication failed unexpectedly", e);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isTenantActive(SystemUser user) {
        if (user.getTenantId() != null) {
            var t = tenantRepo.findById(user.getTenantId());
            if (t.isPresent() && !t.get().isActive()) {
                log.warn("DevAuthFilter: Login blocked — tenant {} is inactive for user {}", user.getTenantId(), user.getUsername());
                return false;
            }
        }
        return true;
    }

    private Authentication authFromUsername(String username) {
        Optional<SystemUser> u = userRepo.findByUsername(username);
        if (u.isPresent()) {
            SystemUser user = u.get();
            if (!user.isActive() || !isTenantActive(user))
                return null;
            String role = normaliseRole(user.getRole());
            return new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                    List.of(new SimpleGrantedAuthority(role)));
        }
        return null;
    }

    private Authentication authFromApiKey(String key) {
        Optional<SystemUser> u = userRepo.findByApiKey(key);
        if (u.isPresent()) {
            SystemUser user = u.get();
            if (!user.isActive() || !isTenantActive(user))
                return null;
            String role = normaliseRole(user.getRole());
            return new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                    List.of(new SimpleGrantedAuthority(role)));
        }
        return null;
    }

    private Authentication authFromBasic(String username, String password) {
        log.debug("DevAuthFilter: Attempting Basic Auth for user: {}", username);

        Optional<SystemUser> u = userRepo.findByUsername(username);
        if (u.isEmpty()) {
            log.debug("DevAuthFilter: User '{}' not found in DB", username);
            return null;
        }

        SystemUser user = u.get();

        if (!user.isActive()) {
            log.warn("DevAuthFilter: Login blocked — user '{}' is inactive", username);
            return null;
        }

        if (!isTenantActive(user)) {
            return null;
        }

        // Verify password with BCrypt
        if (passwordEncoder.matches(password, user.getHashedPassword())) {
            log.debug("DevAuthFilter: Password verified for user '{}', role: {}", username, user.getRole());
            String role = normaliseRole(user.getRole());
            return new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                    List.of(new SimpleGrantedAuthority(role)));
        } else {
            log.warn("DevAuthFilter: Password mismatch for user '{}'", username);
            return null;
        }
    }

    /** Ensures role always has the ROLE_ prefix expected by Spring Security. */
    private String normaliseRole(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}
