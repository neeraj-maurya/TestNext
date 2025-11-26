package com.testnext.api.dto;

public class ProjectDto {
    public Long id;
    public Long tenantId;
    public String name;
    public String description;

    public ProjectDto() {}

    public ProjectDto(Long id, Long tenantId, String name, String description) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.description = description;
    }
}
