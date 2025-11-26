package com.testnext.api.controller;

import com.testnext.api.dto.TestSuiteDto;
import com.testnext.service.TestSuiteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/test-suites")
public class TestSuiteController {
    private final TestSuiteService svc;

    public TestSuiteController(TestSuiteService svc) { this.svc = svc; }

    @PostMapping
    public TestSuiteDto create(@PathVariable Long projectId, @RequestBody TestSuiteDto in) {
        return svc.create(projectId, in.name, in.description);
    }
}
