package com.testnext.security;

import com.testnext.tenant.entity.TenantEntity;
import com.testnext.repository.TenantRepository;
import com.testnext.user.SystemUser;
import com.testnext.user.SystemUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("tenantSecurity")
public class TenantSecurityService {

    private final TenantRepository tenantRepo;
    private final SystemUserRepository userRepo;

    public TenantSecurityService(TenantRepository tenantRepo, SystemUserRepository userRepo) {
        this.tenantRepo = tenantRepo;
        this.userRepo = userRepo;
    }

    public boolean isMember(Authentication auth, Long tenantId) {
        if (auth == null || !auth.isAuthenticated())
            return false;

        // Assuming getUser and isAdmin methods exist or will be added elsewhere for
        // compilation
        // For the purpose of this edit, we'll assume their existence or provide minimal
        // placeholders if necessary
        // Based on the original code, we can infer how to get the user and check admin
        // role.
        Optional<SystemUser> userOpt = userRepo.findByUsername(auth.getName());
        if (userOpt.isEmpty()) {
            return false;
        }
        SystemUser user = userOpt.get();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
        if (isAdmin) {
            return true;
        }

        return tenantId != null && tenantId.equals(user.getTenantId());
    }

    public boolean hasAccess(Authentication auth, Long tenantId) {
        if (auth == null || !auth.isAuthenticated())
            return false;

        // System Admin has access to everything
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
        if (isAdmin)
            return true;

        // Check if user is Test Manager of this tenant
        String username = auth.getName();
        Optional<SystemUser> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty())
            return false;

        SystemUser user = userOpt.get();
        // Must be Test Manager
        if (!"TEST_MANAGER".equals(user.getRole()) && !"ROLE_TEST_MANAGER".equals(user.getRole())) {
            return false;
        }

        Optional<TenantEntity> tenantOpt = tenantRepo.findById(tenantId);
        if (tenantOpt.isEmpty())
            return false;

        TenantEntity tenant = tenantOpt.get();
        return user.getId().equals(tenant.getTestManagerId());
    }

    public boolean isTestManagerForTenant(Authentication auth, Long tenantId) {
        return hasAccess(auth, tenantId);
    }
}
