package com.testnext.service;

import com.testnext.api.dto.ProjectDto;
import com.testnext.project.entity.ProjectEntity;
import com.testnext.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectRepository repo;

    public ProjectService(ProjectRepository repo) {
        this.repo = repo;
    }

    public ProjectDto create(Long tenantId, String name, String description) {
        ProjectEntity e = new ProjectEntity();
        e.setTenantId(tenantId);
        e.setName(name);
        e.setDescription(description);
        ProjectEntity saved = repo.save(e);
        return new ProjectDto(saved.getId(), saved.getTenantId(), saved.getName(), saved.getDescription());
    }

    public List<ProjectDto> listByTenant(Long tenantId) {
        return repo.findByTenantId(tenantId).stream()
                .map(e -> new ProjectDto(e.getId(), e.getTenantId(), e.getName(), e.getDescription()))
                .collect(Collectors.toList());
    }
}
