package com.testnext.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_assignments", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "user_id")
    private java.util.Set<java.util.UUID> assignedUserIds = new java.util.HashSet<>();

    public java.util.Set<java.util.UUID> getAssignedUserIds() {
        return assignedUserIds;
    }

    public void setAssignedUserIds(java.util.Set<java.util.UUID> assignedUserIds) {
        this.assignedUserIds = assignedUserIds;
    }

    public ProjectEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
