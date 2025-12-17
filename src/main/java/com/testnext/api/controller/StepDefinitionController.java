package com.testnext.api.controller;

import com.testnext.api.dto.StepDefinitionDto;
import com.testnext.service.StepDefinitionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/test-steps-library")
public class StepDefinitionController {
    private final StepDefinitionService svc;

    public StepDefinitionController(StepDefinitionService svc) {
        this.svc = svc;
    }

    @PostMapping
    public StepDefinitionDto create(@PathVariable Long tenantId, @RequestBody StepDefinitionDto in) {
        // tenantId ignored for now
        return svc.create(in);
    }

    @GetMapping
    public List<StepDefinitionDto> list(@PathVariable Long tenantId) {
        return svc.list();
    }
}
