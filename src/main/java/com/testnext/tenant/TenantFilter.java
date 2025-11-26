package com.testnext.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

// Note: wiring into security filter chain and JWT parsing must be implemented in your project
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Example resolution strategies (choose one or combine):
            // 1) From JWT claim "tenant_schema"
            // 2) From subdomain in Host header
            // 3) From custom header X-Tenant

            String tenant = resolveTenantFromHeaderOrJwt(request);

            if (tenant == null || tenant.isEmpty()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tenant not specified");
                return;
            }

            // IMPORTANT: validate tenant against public.admin_tenants before setting
            TenantContext.setTenant(tenant);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenantFromHeaderOrJwt(HttpServletRequest request) {
        // Placeholder: implement JWT parsing or header/subdomain logic
        String header = request.getHeader("X-Tenant-Schema");
        if (header != null && !header.isEmpty()) return header;
        // Try to resolve from Spring Security Authentication (JWT-based)
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                Object principal = auth.getPrincipal();
                if (principal instanceof Jwt jwt) {
                    Object claim = jwt.getClaim("tenant_schema");
                    if (claim instanceof String && !((String) claim).isEmpty()) return (String) claim;
                    claim = jwt.getClaim("tenant");
                    if (claim instanceof String && !((String) claim).isEmpty()) return (String) claim;
                }

                // Some authentication setups place claims/details in the auth.getDetails()
                Object details = auth.getDetails();
                if (details instanceof Map) {
                    Map<?,?> m = (Map<?,?>) details;
                    Object t = m.get("tenant_schema");
                    if (t instanceof String && !((String) t).isEmpty()) return (String) t;
                    t = m.get("tenant");
                    if (t instanceof String && !((String) t).isEmpty()) return (String) t;
                }
            }
        } catch (Exception ignored) {
            // don't fail tenant resolution on unexpected auth parsing issues
        }

        // Fallback: attempt to infer from host subdomain (e.g. tenant.example.com)
        String host = request.getHeader("Host");
        if (host != null && !host.isEmpty()) {
            String hostOnly = host.split(":")[0];
            if (hostOnly.contains(".")) {
                String sub = hostOnly.split("\\.")[0];
                if (sub != null && !sub.isEmpty() && !sub.equalsIgnoreCase("localhost")) return sub;
            }
        }

        return null;
    }
}
