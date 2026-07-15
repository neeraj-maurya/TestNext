package com.testnext.tenant.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tenants")
public class TenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "schema_name", nullable = false, unique = true)
    private String schemaName;

    @Column(name = "tenant_manager_id")
    private java.util.UUID tenantManagerId;

    @Column(nullable = false)
    private boolean active = true;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public TenantEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public java.util.UUID getTenantManagerId() {
        return tenantManagerId;
    }

    public void setTenantManagerId(java.util.UUID tenantManagerId) {
        this.tenantManagerId = tenantManagerId;
    }
}
