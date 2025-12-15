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
 * Development-friendly authentication filter.
 * Accepts `X-TestNext-User` header to impersonate any user.
 * Also accepts `x-api-key` or Basic auth for legacy/simple tests.
 */
@Component
public class DevAuthFilter extends OncePerRequestFilter {

    private final SystemUserRepository userRepo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public DevAuthFilter(SystemUserRepository userRepo,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        System.out.println("DevAuthFilter: Initialized!");
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DevAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("DevAuthFilter: Processing {} {}", request.getMethod(), request.getRequestURI());
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.info("Header: {} = {}", headerName, request.getHeader(headerName));
        }

        try {
            // 1. Check for User Switching Header
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

    private Authentication authFromUsername(String username) {
        // Hardcoded checks removed. Only DB users allowed.

        Optional<SystemUser> u = userRepo.findByUsername(username);
        if (u.isPresent()) {
            SystemUser user = u.get();
            String role = user.getRole();
            if (!role.startsWith("ROLE_"))
                role = "ROLE_" + role;
            return new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                    List.of(new SimpleGrantedAuthority(role)));
        }
        return null;
    }

    private Authentication authFromApiKey(String key) {
        Optional<SystemUser> u = userRepo.findByApiKey(key);
        if (u.isPresent()) {
            SystemUser user = u.get();
            String role = user.getRole();
            if (!role.startsWith("ROLE_"))
                role = "ROLE_" + role;
            return new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                    List.of(new SimpleGrantedAuthority(role)));
        }
        return null;
    }

    private Authentication authFromBasic(String username, String password) {
        System.out.println("DevAuthFilter: Attempting Basic Auth for user: " + username);

        // Hardcoded bootstrap logic removed.

        Optional<SystemUser> u = userRepo.findByUsername(username);
        if (u.isPresent()) {
            SystemUser user = u.get();
            System.out.println("DevAuthFilter: User found in DB. Role: " + user.getRole());

            // Plain text password comparison as requested
            if (password.equals(user.getHashedPassword())) {
                System.out.println("DevAuthFilter: Password matches (plain text)");
                String role = user.getRole();
                if (!role.startsWith("ROLE_"))
                    role = "ROLE_" + role;
                return new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                        List.of(new SimpleGrantedAuthority(role)));
            } else {
                System.out.println("DevAuthFilter: Password mismatch. Stored: " + user.getHashedPassword()
                        + ", Provided: " + password);
                return null;
            }
        } else {
            System.out.println("DevAuthFilter: User not found in DB");
        }
        return null;
    }
}
