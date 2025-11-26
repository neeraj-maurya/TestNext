package com.testnext.api.controller;

import com.testnext.api.dto.ProjectDto;
import com.testnext.service.ProjectService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/projects")
public class ProjectController {
    private final ProjectService svc;

    public ProjectController(ProjectService svc) {
        this.svc = svc;
    }

    @PostMapping
    public ProjectDto create(@PathVariable Long tenantId, @RequestBody ProjectDto in) {
        return svc.create(tenantId, in.name, in.description);
    }

    @GetMapping
    public List<ProjectDto> list(@PathVariable Long tenantId) { return svc.listByTenant(tenantId); }
}
