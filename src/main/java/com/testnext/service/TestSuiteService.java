package com.testnext.service;

import com.testnext.api.dto.TestSuiteDto;
import com.testnext.model.TestSuiteEntity;
import com.testnext.repository.TestSuiteRepository;
import org.springframework.stereotype.Service;

@Service
public class TestSuiteService {
    private final TestSuiteRepository repo;

    public TestSuiteService(TestSuiteRepository repo) {
        this.repo = repo;
    }

    public TestSuiteDto create(Long projectId, String name, String description) {
        TestSuiteEntity e = new TestSuiteEntity();
        e.projectId = projectId; e.name = name; e.description = description;
        e = repo.save(e);
        TestSuiteDto s = new TestSuiteDto(); s.id = e.id; s.projectId = e.projectId; s.name = e.name; s.description = e.description; return s;
    }
}
