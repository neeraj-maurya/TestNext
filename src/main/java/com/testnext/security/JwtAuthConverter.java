package com.testnext.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Converts JWT 'roles' or 'authorities' claim into GrantedAuthority collection
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {



    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Create authorities from 'roles' claim or 'authorities' claim
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        JwtAuthenticationConverter j = new JwtAuthenticationConverter();
        j.setJwtGrantedAuthoritiesConverter(token -> authorities);
        return j.convert(jwt);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> out = new ArrayList<>();
        Object roles = jwt.getClaimAsMap("realm_access") != null ? jwt.getClaimAsMap("realm_access").get("roles") : null;
        if (roles instanceof List) {
            for (Object r : (List<?>)roles) {
                out.add(new SimpleGrantedAuthority(r.toString()));
            }
        }
        // Also check flat claim 'roles' or 'authorities'
        Object flat = jwt.getClaims().get("roles");
        if (flat instanceof List) {
            for (Object r : (List<?>)flat) out.add(new SimpleGrantedAuthority(r.toString()));
        }
        Object auths = jwt.getClaims().get("authorities");
        if (auths instanceof List) {
            for (Object r : (List<?>)auths) out.add(new SimpleGrantedAuthority(r.toString()));
        }
        return out;
    }
}
