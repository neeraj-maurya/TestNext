package com.testnext.api.dto;

public class TenantDto {
    public Long id;
    public String name;
    public String schemaName;

    public java.util.UUID testManagerId;

    public TenantDto() {
    }

    public TenantDto(Long id, String name, String schemaName) {
        this.id = id;
        this.name = name;
        this.schemaName = schemaName;
    }

    public TenantDto(Long id, String name, String schemaName, java.util.UUID testManagerId) {
        this.id = id;
        this.name = name;
        this.schemaName = schemaName;
        this.testManagerId = testManagerId;
    }
}
