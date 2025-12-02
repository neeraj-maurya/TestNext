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

    public ProjectController(ProjectService svc) {
        this.svc = svc;
    }

    @PostMapping
    @PreAuthorize("@tenantSecurity.hasAccess(authentication, #tenantId)")
    public ProjectDto create(@PathVariable Long tenantId, @RequestBody ProjectDto in) {
        return svc.create(tenantId, in.name, in.description);
    }

    @GetMapping
    @PreAuthorize("@tenantSecurity.hasAccess(authentication, #tenantId)")
    public List<ProjectDto> list(@PathVariable Long tenantId) {
        return svc.listByTenant(tenantId);
    }
}
