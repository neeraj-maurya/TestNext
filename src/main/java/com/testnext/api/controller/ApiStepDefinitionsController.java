package com.testnext.api.controller;

import com.testnext.api.dto.StepDefinitionDto;
import com.testnext.service.StepDefinitionService;
import com.testnext.repository.StepDefinitionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/step-definitions")
public class ApiStepDefinitionsController {
    private final StepDefinitionService svc;
    private final StepDefinitionRepository repo;

    public ApiStepDefinitionsController(StepDefinitionService svc, StepDefinitionRepository repo) {
        this.svc = svc;
        this.repo = repo;
    }

    @GetMapping
    public List<StepDefinitionDto> list() {
        return svc.list();
    }

    @PostMapping
    public StepDefinitionDto create(@RequestBody StepDefinitionDto in) {
        return svc.create(in);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @PostMapping("/load-runtime")
    public List<StepDefinitionDto> loadRuntimeSteps() {
        // Simulate loading steps from an external source or runtime discovery
        createIfMissing("Runtime: Verify Element", "Verify that an element exists on the page");
        createIfMissing("Runtime: Click Button", "Click a button identified by selector");
        createIfMissing("Runtime: Enter Text", "Enter text into an input field");
        return svc.list();
    }

    private void createIfMissing(String name, String definition) {
        if (svc.list().stream().noneMatch(s -> s.name.equals(name))) {
            StepDefinitionDto dto = new StepDefinitionDto();
            dto.name = name;
            dto.description = definition;
            svc.create(dto);
        }
    }
}
