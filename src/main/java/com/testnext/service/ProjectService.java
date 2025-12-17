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

    public List<ProjectDto> listByTenant(Long tenantId, String role, java.util.UUID userId) {
        // If user is Admin or Test Manager, return all. Else filter by assignment.
        // Assuming "ROLE_SYSTEM_ADMIN", "ROLE_TEST_MANAGER"
        boolean canViewAll = "ROLE_SYSTEM_ADMIN".equals(role) || "ROLE_TEST_MANAGER".equals(role);

        return repo.findByTenantId(tenantId).stream()
                .filter(p -> canViewAll || p.getAssignedUserIds().contains(userId))
                .map(e -> new ProjectDto(e.getId(), e.getTenantId(), e.getName(), e.getDescription()))
                .collect(Collectors.toList());
    }

    public void updateAssignments(Long projectId, java.util.List<java.util.UUID> userIds) {
        ProjectEntity p = repo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        p.setAssignedUserIds(new java.util.HashSet<>(userIds));
        repo.save(p);
    }
}
