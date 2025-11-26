package com.testnext.provisioning.dto;

import java.util.UUID;

public class CreateTenantResponse {
    private UUID tenantId;
    private String schemaName;
    private UUID adminInvitationId;
    private String message;

    // getters/setters
    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }

    public String getSchemaName() { return schemaName; }
    public void setSchemaName(String schemaName) { this.schemaName = schemaName; }

    public UUID getAdminInvitationId() { return adminInvitationId; }
    public void setAdminInvitationId(UUID adminInvitationId) { this.adminInvitationId = adminInvitationId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
