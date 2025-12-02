package com.testnext.api.controller;

import com.testnext.api.dto.StepDefinitionDto;
import com.testnext.service.StepDefinitionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/step-definitions")
public class GlobalStepDefinitionController {
    private final StepDefinitionService svc;

    public GlobalStepDefinitionController(StepDefinitionService svc) {
        this.svc = svc;
    }

    @GetMapping
    public List<StepDefinitionDto> list() {
        return svc.list();
    }

    @PostMapping
    public StepDefinitionDto create(@RequestBody StepDefinitionDto in) {
        return svc.create(in);
    }

    @PostMapping("/load-runtime")
    public List<StepDefinitionDto> loadRuntime() {
        // Placeholder for loading runtime steps
        return svc.list();
    }
}
