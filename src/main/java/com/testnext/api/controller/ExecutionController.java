package com.testnext.api.controller;

import com.testnext.api.dto.ExecutionDto;
import com.testnext.service.ExecutionService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class ExecutionController {
    private final ExecutionService svc;

    public ExecutionController(ExecutionService svc) {
        this.svc = svc;
    }

    @PostMapping("/api/tests/{testId}/executions")
    public ExecutionDto start(@PathVariable Long testId, @RequestBody(required = false) Map<String, Object> body) {
        Long runFrom = null;
        if (body != null && body.containsKey("runFromStepId"))
            runFrom = ((Number) body.get("runFromStepId")).longValue();
        return svc.start(testId, runFrom);
    }

    @PostMapping("/api/executions")
    public java.util.List<ExecutionDto> startSuite(@RequestBody Map<String, Long> body) {
        return svc.startSuite(body.get("suiteId"));
    }

    @GetMapping("/api/executions")
    public java.util.List<ExecutionDto> list(org.springframework.security.core.Authentication auth) {
        return svc.list(auth);
    }

    @GetMapping("/api/executions/{executionId}")
    public ExecutionDto get(@PathVariable java.util.UUID executionId) {
        return svc.get(executionId);
    }

    @GetMapping("/api/projects/{projectId}/executions")
    public java.util.List<ExecutionDto> listByProject(@PathVariable Long projectId) {
        return svc.listByProject(projectId);
    }

    @DeleteMapping("/api/executions/{executionId}")
    @org.springframework.security.access.prepost.PreAuthorize("@executionSecurity.canDelete(authentication, #executionId)")
    public void delete(@PathVariable java.util.UUID executionId) {
        svc.delete(executionId);
    }
}
