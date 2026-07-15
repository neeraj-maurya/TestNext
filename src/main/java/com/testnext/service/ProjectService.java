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
        e.setName(name);
        e.setDescription(description);
        e.setTenantId(tenantId);
        ProjectEntity saved = repo.save(e);
        return new ProjectDto(saved.getId(), saved.getName(), saved.getDescription(), saved.getTenantId(), saved.getProjectManagerId());
    }

    public List<ProjectDto> listAll(Long tenantId, String role, java.util.UUID userId) {
        // If user is Admin or Tenant Manager, return all for the tenant. Else filter by assignment.
        boolean canViewAll = "ROLE_SYSTEM_ADMIN".equals(role) || "ROLE_TENANT_MANAGER".equals(role) || "TENANT_MANAGER".equals(role);

        return repo.findAll().stream()
                .filter(p -> p.getTenantId() != null && p.getTenantId().equals(tenantId))
                .filter(p -> canViewAll || p.getAssignedUserIds().contains(userId) || (p.getProjectManagerId() != null && p.getProjectManagerId().equals(userId)))
                .map(e -> new ProjectDto(e.getId(), e.getName(), e.getDescription(), e.getTenantId(), e.getProjectManagerId()))
                .collect(Collectors.toList());
    }

    public void updateProjectManager(Long projectId, java.util.UUID projectManagerId) {
        ProjectEntity p = repo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        p.setProjectManagerId(projectManagerId);
        repo.save(p);
    }

    public void delete(Long projectId) {
        repo.deleteById(projectId);
    }

    public void updateAssignments(Long projectId, java.util.List<java.util.UUID> userIds) {
        ProjectEntity p = repo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        p.setAssignedUserIds(new java.util.HashSet<>(userIds));
        repo.save(p);
    }
}
