package com.testnext.api.dto;

public class TenantDto {
    public Long id;
    public String name;
    public String schemaName;

    public java.util.UUID tenantManagerId;
    public boolean active = true;

    public TenantDto() {
    }

    public TenantDto(Long id, String name, String schemaName) {
        this.id = id;
        this.name = name;
        this.schemaName = schemaName;
    }

    public TenantDto(Long id, String name, String schemaName, java.util.UUID tenantManagerId) {
        this(id, name, schemaName, tenantManagerId, true);
    }

    public TenantDto(Long id, String name, String schemaName, java.util.UUID tenantManagerId, boolean active) {
        this.id = id;
        this.name = name;
        this.schemaName = schemaName;
        this.tenantManagerId = tenantManagerId;
        this.active = active;
    }
}
