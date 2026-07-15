package com.testnext.api.dto;

public class ProjectDto {
    public Long id;
    public String name;
    public String description;

    public Long tenantId;
    public java.util.UUID projectManagerId;

    public ProjectDto() {}

    public ProjectDto(Long id, String name, String description, Long tenantId, java.util.UUID projectManagerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.tenantId = tenantId;
        this.projectManagerId = projectManagerId;
    }
}
