package com.testnext.service;

import com.testnext.api.dto.TestDto;
import com.testnext.model.TestEntity;
import com.testnext.model.TestStepEntity;
import com.testnext.repository.TestRepository;
import com.testnext.repository.TestStepRepository;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    private final TestRepository repo;
    private final TestStepRepository stepRepo;

    public TestService(TestRepository repo, TestStepRepository stepRepo) {
        this.repo = repo; this.stepRepo = stepRepo;
    }

    public TestDto create(Long suiteId, TestDto in) {
        TestEntity e = new TestEntity(); e.suiteId = suiteId; e.name = in.name;
        e = repo.save(e);
        // persist steps if provided
        if (in.steps != null) {
            for (TestDto.TestStepDto s : in.steps) {
                TestStepEntity tse = new TestStepEntity();
                tse.testId = e.id; tse.stepDefinitionId = s.stepDefinitionId; tse.parametersJson = s.parameters == null ? null : s.parameters.toString();
                stepRepo.save(tse);
            }
        }
        TestDto out = new TestDto(); out.id = e.id; out.suiteId = e.suiteId; out.name = e.name; return out;
    }
}
