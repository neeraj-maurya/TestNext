package com.testnext.api.controller;

import com.testnext.api.dto.TestDto;
import com.testnext.service.TestService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-suites/{suiteId}/tests")
public class TestController {
    private final TestService svc;

    public TestController(TestService svc) { this.svc = svc; }

    @PostMapping
    public TestDto create(@PathVariable Long suiteId, @RequestBody TestDto in) {
        return svc.create(suiteId, in);
    }
}
