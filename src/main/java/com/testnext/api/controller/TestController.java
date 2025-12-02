package com.testnext.api.controller;

import com.testnext.api.dto.TestDto;
import com.testnext.service.TestService;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {
    private final TestService svc;

    public TestController(TestService svc) {
        this.svc = svc;
    }

    @PostMapping("/api/test-suites/{suiteId}/tests")
    public TestDto create(@PathVariable Long suiteId, @RequestBody TestDto in) {
        return svc.create(suiteId, in);
    }

    @GetMapping("/api/test-suites/{suiteId}/tests")
    public java.util.List<TestDto> list(@PathVariable Long suiteId) {
        return svc.list(suiteId);
    }

    @GetMapping("/api/projects/{projectId}/tests")
    public java.util.List<TestDto> listByProject(@PathVariable Long projectId) {
        return svc.listByProject(projectId);
    }

    @DeleteMapping("/api/tests/{testId}")
    @org.springframework.security.access.prepost.PreAuthorize("@testSecurity.canDelete(authentication, #testId)")
    public void delete(@PathVariable Long testId) {
        svc.delete(testId);
    }
}
