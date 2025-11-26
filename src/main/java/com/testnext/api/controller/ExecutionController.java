package com.testnext.api.controller;

import com.testnext.api.dto.ExecutionDto;
import com.testnext.service.ExecutionService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class ExecutionController {
    private final ExecutionService svc;

    public ExecutionController(ExecutionService svc) { this.svc = svc; }

    @PostMapping("/api/tests/{testId}/executions")
    public ExecutionDto start(@PathVariable Long testId, @RequestBody(required = false) Map<String, Object> body) {
        Long runFrom = null;
        if (body != null && body.containsKey("runFromStepId")) runFrom = ((Number)body.get("runFromStepId")).longValue();
        return svc.start(testId, runFrom);
    }

    @GetMapping("/api/executions/{executionId}")
    public ExecutionDto get(@PathVariable java.util.UUID executionId) { return svc.get(executionId); }
}
