package com.testnext.provisioning;

import com.testnext.provisioning.dto.CreateTenantRequest;
import com.testnext.provisioning.dto.CreateTenantResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TenantProvisioningService {

    private final JdbcTemplate centralJdbc; // connected to public schema
    private final TenantMigrationRunner migrationRunner;

    public TenantProvisioningService(JdbcTemplate centralJdbc, TenantMigrationRunner migrationRunner) {
        this.centralJdbc = centralJdbc;
        this.migrationRunner = migrationRunner;
    }

    @Transactional
    public CreateTenantResponse createTenant(CreateTenantRequest req, UUID createdBy) {
        // Validate inputs
        String schema = generateSchemaName(req.getPreferredSchemaName(), req.getName());
        // Check uniqueness
        int existing = centralJdbc.queryForObject("select count(1) from public.admin_tenants where schema_name = ?", Integer.class, schema);
        if (existing > 0) throw new IllegalArgumentException("schema name already exists");

        UUID tenantId = UUID.randomUUID();

        // Insert into admin_tenants
        centralJdbc.update("insert into public.admin_tenants (id, name, domain, schema_name, created_by) values (?, ?, ?, ?, ?)",
                tenantId, req.getName(), req.getDomain(), schema, createdBy);

        // Create schema and run migrations
        migrationRunner.createSchemaAndMigrate(schema);

        // Seed default step definitions (in tenant schema) via migration runner
        migrationRunner.seedStepDefinitions(schema);

        // Create invitation token for admin user
        UUID invitationId = UUID.randomUUID();
        String token = UUID.randomUUID().toString();
        centralJdbc.update("insert into public.tenant_invitations (id, tenant_id, email, token, invited_by, expires_at) values (?, ?, ?, ?, ?, now() + interval '7 days')",
                invitationId, tenantId, req.getAdminEmail(), token, createdBy);

        CreateTenantResponse resp = new CreateTenantResponse();
        resp.setTenantId(tenantId);
        resp.setSchemaName(schema);
        resp.setAdminInvitationId(invitationId);
        resp.setMessage("Tenant created; invitation sent to admin email (token). Replace this with real email flow.");
        return resp;
    }

    private String generateSchemaName(String preferred, String name) {
        if (preferred != null && !preferred.isBlank()) return sanitizeSchema(preferred);
        // simple slugify
        String s = name.toLowerCase().replaceAll("[^a-z0-9]+", "_");
        if (s.length() > 40) s = s.substring(0,40);
        return sanitizeSchema(s + "_testnext");
    }

    private String sanitizeSchema(String s) {
        // allow only a-z0-9_ and start with letter
        s = s.toLowerCase().replaceAll("[^a-z0-9_]+", "_");
        if (!s.matches("^[a-z].*")) s = "t_" + s;
        return s;
    }
}
