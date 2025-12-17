package com.testnext.api.dto;

public class TenantDto {
    public Long id;
    public String name;
    public String schemaName;

    public java.util.UUID testManagerId;
    public boolean active = true;

    public TenantDto() {
    }

    public TenantDto(Long id, String name, String schemaName) {
        this.id = id;
        this.name = name;
        this.schemaName = schemaName;
    }

    public TenantDto(Long id, String name, String schemaName, java.util.UUID testManagerId) {
        this(id, name, schemaName, testManagerId, true);
    }

    public TenantDto(Long id, String name, String schemaName, java.util.UUID testManagerId, boolean active) {
        this.id = id;
        this.name = name;
        this.schemaName = schemaName;
        this.testManagerId = testManagerId;
        this.active = active;
    }
}
