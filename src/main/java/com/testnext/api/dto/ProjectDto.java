package com.testnext.api.dto;

public class ProjectDto {
    public Long id;
    public String name;
    public String description;

    public ProjectDto() {}

    public ProjectDto(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
