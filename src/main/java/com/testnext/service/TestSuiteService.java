package com.testnext.service;

import com.testnext.api.dto.TestSuiteDto;
import com.testnext.model.TestSuiteEntity;
import com.testnext.repository.TestSuiteRepository;
import org.springframework.stereotype.Service;

@Service
public class TestSuiteService {
    private final TestSuiteRepository repo;
    private final com.testnext.repository.ProjectRepository projectRepo;

    public TestSuiteService(TestSuiteRepository repo, com.testnext.repository.ProjectRepository projectRepo) {
        this.repo = repo;
        this.projectRepo = projectRepo;
    }

    public TestSuiteDto create(Long projectId, String name, String description) {
        TestSuiteEntity e = new TestSuiteEntity();
        e.projectId = projectId;
        e.name = name;
        e.description = description;
        e = repo.save(e);
        return toDto(e);
    }

    public java.util.List<TestSuiteDto> listAll() {
        return repo.findAll().stream().map(this::toDto).collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<TestSuiteDto> listByProject(Long projectId) {
        return repo.findByProjectId(projectId).stream().map(this::toDto).collect(java.util.stream.Collectors.toList());
    }

    public TestSuiteDto update(Long id, String name, String description) {
        TestSuiteEntity e = repo.findById(id).orElseThrow();
        if (name != null)
            e.name = name;
        if (description != null)
            e.description = description;
        e = repo.save(e);
        return toDto(e);
    }

    public TestSuiteDto getById(Long id) {
        return repo.findById(id).map(this::toDto).orElseThrow(() -> new RuntimeException("Suite not found"));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private TestSuiteDto toDto(TestSuiteEntity e) {
        TestSuiteDto s = new TestSuiteDto();
        s.id = e.id;
        s.projectId = e.projectId;
        s.name = e.name;
        s.description = e.description;
        // Fetch tenantId from project
        projectRepo.findById(e.projectId).ifPresent(p -> s.tenantId = p.getTenantId());
        return s;
    }
}
