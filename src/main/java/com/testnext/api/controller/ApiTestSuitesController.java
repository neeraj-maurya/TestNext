package com.testnext.api.controller;

import com.testnext.api.dto.TestSuiteDto;
import com.testnext.model.TestSuiteEntity;
import com.testnext.model.TestEntity;
import com.testnext.repository.TestSuiteRepository;
import com.testnext.repository.TestRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test-suites")
public class ApiTestSuitesController {
    private final TestSuiteRepository suiteRepo;
    private final TestRepository testRepo;

    public ApiTestSuitesController(TestSuiteRepository suiteRepo, TestRepository testRepo) {
        this.suiteRepo = suiteRepo;
        this.testRepo = testRepo;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        var suites = suiteRepo.findAll();
        var tests = testRepo.findAll();
        var counts = tests.stream().collect(Collectors.groupingBy(t -> t.suiteId, Collectors.counting()));
        return suites.stream().map(s -> Map.<String,Object>of(
                "id", s.id,
                "projectId", s.projectId,
                "name", s.name,
                "description", s.description,
                "testCount", counts.getOrDefault(s.id, 0L)
        )).collect(Collectors.toList());
    }

    @PostMapping
    public TestSuiteDto create(@RequestBody TestSuiteDto in) {
        TestSuiteEntity e = new TestSuiteEntity();
        e.projectId = in.projectId == null ? 1L : in.projectId;
        e.name = in.name;
        e.description = in.description;
        e = suiteRepo.save(e);
        TestSuiteDto out = new TestSuiteDto(); out.id = e.id; out.projectId = e.projectId; out.name = e.name; out.description = e.description; return out;
    }

    @PutMapping("/{id}")
    public TestSuiteDto update(@PathVariable Long id, @RequestBody TestSuiteDto in) {
        var opt = suiteRepo.findById(id);
        if (opt.isEmpty()) return null;
        var e = opt.get();
        if (in.name != null) e.name = in.name;
        if (in.description != null) e.description = in.description;
        e = suiteRepo.save(e);
        TestSuiteDto out = new TestSuiteDto(); out.id = e.id; out.projectId = e.projectId; out.name = e.name; out.description = e.description; return out;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { suiteRepo.deleteById(id); }
}
