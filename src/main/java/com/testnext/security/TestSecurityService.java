package com.testnext.security;

import com.testnext.service.TestService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("testSecurity")
public class TestSecurityService {
    private final TestService testService;
    private final TenantSecurityService tenantSecurity;

    public TestSecurityService(TestService testService, TenantSecurityService tenantSecurity) {
        this.testService = testService;
        this.tenantSecurity = tenantSecurity;
    }

    public boolean canDelete(Authentication auth, Long testId) {
        if (auth == null || !auth.isAuthenticated())
            return false;

        // System Admin can delete anything
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
        if (isAdmin)
            return true;

        // Test Manager can delete if they manage the tenant owning the test
        Long tenantId = testService.getTenantIdForTest(testId);
        if (tenantId == null)
            return false; // Orphaned test or not found

        return tenantSecurity.isTestManagerForTenant(auth, tenantId);
    }
}
