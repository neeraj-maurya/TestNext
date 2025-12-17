package com.testnext.api.controller;

import com.testnext.api.dto.ProjectDto;
import com.testnext.service.ProjectService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/projects")
public class ProjectController {
    private final ProjectService svc;
    private final com.testnext.user.SystemUserRepository userRepo;

    public ProjectController(ProjectService svc, com.testnext.user.SystemUserRepository userRepo) {
        this.svc = svc;
        this.userRepo = userRepo;
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ProjectDto create(@PathVariable Long tenantId, @RequestBody ProjectDto in) {
        return svc.create(tenantId, in.name, in.description);
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.isMember(authentication, #tenantId)")
    public List<ProjectDto> list(@PathVariable Long tenantId, org.springframework.security.core.Authentication auth) {
        String username = auth.getName();
        String role = auth.getAuthorities().stream().findFirst().map(Object::toString).orElse("");
        return svc.listByTenant(tenantId, role, getUserId(auth));
    }

    @PutMapping("/{projectId}/assignments")
    @PreAuthorize("@tenantSecurity.isManager(authentication, #tenantId)")
    public void updateAssignments(@PathVariable Long tenantId, @PathVariable Long projectId,
            @RequestBody List<java.util.UUID> userIds) {
        svc.updateAssignments(projectId, userIds);
    }

    // Quick helper to fetch ID - In real app, put in base controller or util
    // I need to inject Repo to do this properly.
    private java.util.UUID getUserId(org.springframework.security.core.Authentication auth) {
        if (auth == null)
            return null;
        String username = auth.getName();
        return userRepo.findByUsername(username)
                .map(com.testnext.user.SystemUser::getId)
                .orElse(null);
    }
}
