package com.testnext.api.dto;

public class TenantDto {
    public Long id;
    public String name;
    public String schemaName;

    public TenantDto() {}

    public TenantDto(Long id, String name, String schemaName) {
        this.id = id;
        this.name = name;
        this.schemaName = schemaName;
    }
}
