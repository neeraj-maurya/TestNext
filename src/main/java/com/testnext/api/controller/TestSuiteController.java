package com.testnext.api.controller;

import com.testnext.api.dto.TestSuiteDto;
import com.testnext.service.TestSuiteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/suites")
public class TestSuiteController {

    private final TestSuiteService svc;

    public TestSuiteController(TestSuiteService svc) {
        this.svc = svc;
    }

    @PostMapping
    @PreAuthorize("@projectSecurity.hasAccess(authentication, #projectId)")
    public TestSuiteDto create(@PathVariable Long projectId, @RequestBody TestSuiteDto in) {
        return svc.create(projectId, in.name, in.description);
    }

    @GetMapping
    @PreAuthorize("@projectSecurity.hasAccess(authentication, #projectId)")
    public List<TestSuiteDto> list(@PathVariable Long projectId) {
        return svc.listByProject(projectId);
    }

    @GetMapping("/{suiteId}")
    @PreAuthorize("@projectSecurity.hasAccess(authentication, #projectId)")
    public TestSuiteDto get(@PathVariable Long projectId, @PathVariable Long suiteId) {
        return svc.getById(suiteId);
    }
}
