package com.testnext.tenant;

import java.util.Optional;

public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenant(String tenantSchema) {
        currentTenant.set(tenantSchema);
    }

    public static Optional<String> getTenant() {
        return Optional.ofNullable(currentTenant.get());
    }

    public static void clear() {
        currentTenant.remove();
    }
}
