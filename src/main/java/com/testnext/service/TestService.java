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
    private final com.testnext.repository.TestSuiteRepository suiteRepo;
    private final com.testnext.repository.ProjectRepository projectRepo;

    public TestService(
            TestRepository repo,
            TestStepRepository stepRepo,
            com.testnext.repository.TestSuiteRepository suiteRepo,
            com.testnext.repository.ProjectRepository projectRepo) {
        this.repo = repo;
        this.stepRepo = stepRepo;
        this.suiteRepo = suiteRepo;
        this.projectRepo = projectRepo;
    }

    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public TestDto create(Long suiteId, TestDto in) {
        TestEntity e = new TestEntity();
        e.suiteId = suiteId;
        e.name = in.name;
        e = repo.save(e);
        // persist steps if provided
        if (in.steps != null) {
            for (TestDto.TestStepDto s : in.steps) {
                TestStepEntity tse = new TestStepEntity();
                tse.testId = e.id;
                tse.stepDefinitionId = s.stepDefinitionId;
                try {
                    tse.parametersJson = s.parameters == null ? null : mapper.writeValueAsString(s.parameters);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                stepRepo.save(tse);
            }
        }
        TestDto out = new TestDto();
        out.id = e.id;
        out.suiteId = e.suiteId;
        out.name = e.name;
        return out;
    }

    public java.util.List<TestDto> list(Long suiteId) {
        return repo.findBySuiteId(suiteId).stream().map(e -> {
            TestDto dto = new TestDto();
            dto.id = e.id;
            dto.suiteId = e.suiteId;
            dto.name = e.name;
            // TODO: fetch steps
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    public void delete(Long id) {
        stepRepo.deleteAll(stepRepo.findByTestIdOrderById(id));
        repo.deleteById(id);
    }

    public Long getTenantIdForTest(Long testId) {
        TestEntity t = repo.findById(testId).orElse(null);
        if (t == null)
            return null;
        com.testnext.model.TestSuiteEntity s = suiteRepo.findById(t.suiteId).orElse(null);
        if (s == null)
            return null;
        return projectRepo.findById(s.projectId).map(p -> p.getTenantId()).orElse(null);
    }

    public java.util.List<TestDto> listByProject(Long projectId) {
        return repo.findByProjectId(projectId).stream().map(e -> {
            TestDto dto = new TestDto();
            dto.id = e.id;
            dto.suiteId = e.suiteId;
            dto.name = e.name;
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }
}
