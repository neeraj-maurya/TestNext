package com.testnext.security;

import com.testnext.tenant.TenantContext;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Extracts tenant schema from JWT claim 'tenant_schema' or from header 'X-Tenant-Schema' and sets TenantContext.
 * Runs after authentication is established.
 */
@Component
public class TenantExtractionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            String tenant = null;
            // Header takes precedence
            String header = request.getHeader("X-Tenant-Schema");
            if (header != null && !header.isBlank()) {
                tenant = header;
            } else if (auth instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) auth;
                Object claim = jwtToken.getToken().getClaims().get("tenant_schema");
                if (claim != null) tenant = claim.toString();
            }

            if (tenant != null && !tenant.isBlank()) {
                // sanitize tenant name in service layer when using to set search_path
                TenantContext.setTenant(tenant);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
