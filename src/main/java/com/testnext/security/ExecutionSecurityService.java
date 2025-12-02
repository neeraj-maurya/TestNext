package com.testnext.security;

import com.testnext.service.ExecutionService;
import com.testnext.tenant.entity.TenantEntity;
import com.testnext.repository.TenantRepository;
import com.testnext.user.SystemUser;
import com.testnext.user.SystemUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service("executionSecurity")
public class ExecutionSecurityService {

    private final ExecutionService executionService;
    private final TenantRepository tenantRepo;
    private final SystemUserRepository userRepo;

    public ExecutionSecurityService(ExecutionService executionService, TenantRepository tenantRepo,
            SystemUserRepository userRepo) {
        this.executionService = executionService;
        this.tenantRepo = tenantRepo;
        this.userRepo = userRepo;
    }

    public boolean canDelete(Authentication auth, UUID executionId) {
        if (auth == null || !auth.isAuthenticated())
            return false;

        // System Admin has access to everything
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
        if (isAdmin)
            return true;

        // Get Tenant ID for this execution
        Long tenantId = executionService.getTenantIdForExecution(executionId);
        if (tenantId == null)
            return false; // Orphaned execution or not found

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
}
