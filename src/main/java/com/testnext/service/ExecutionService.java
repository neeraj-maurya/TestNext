package com.testnext.service;

import com.testnext.api.dto.ExecutionDto;
import com.testnext.model.ExecutionEntity;
import com.testnext.model.ExecutionStepEntity;
import com.testnext.model.TestStepEntity;
import com.testnext.observability.ObservabilityConfig;
import com.testnext.repository.ExecutionRepository;
import com.testnext.repository.ExecutionStepRepository;
import com.testnext.repository.TestStepRepository;
import io.micrometer.core.instrument.Timer;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ExecutionService {
    private final TestStepRepository testStepRepo;
    private final ExecutionRepository execRepo;
    private final ExecutionStepRepository execStepRepo;
    private final com.testnext.repository.StepDefinitionRepository stepDefRepo;
    private final com.testnext.repository.TestRepository testRepo;
    private final com.testnext.repository.TestSuiteRepository suiteRepo;
    private final com.testnext.execution.SystemStepExecutor stepExecutor;
    private final ObservabilityConfig.ExecutionMetrics metrics;
    private final com.testnext.repository.ProjectRepository projectRepo;
    private final com.testnext.user.SystemUserRepository userRepo;
    private final com.testnext.repository.TenantRepository tenantRepo;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public ExecutionService(
            TestStepRepository testStepRepo,
            ExecutionRepository execRepo,
            ExecutionStepRepository execStepRepo,
            com.testnext.repository.StepDefinitionRepository stepDefRepo,
            com.testnext.repository.TestRepository testRepo,
            com.testnext.repository.TestSuiteRepository suiteRepo,
            com.testnext.repository.ProjectRepository projectRepo,
            com.testnext.user.SystemUserRepository userRepo,
            com.testnext.repository.TenantRepository tenantRepo,
            com.testnext.execution.SystemStepExecutor stepExecutor,
            ObservabilityConfig.ExecutionMetrics metrics) {
        this.testStepRepo = testStepRepo;
        this.execRepo = execRepo;
        this.execStepRepo = execStepRepo;
        this.stepDefRepo = stepDefRepo;
        this.testRepo = testRepo;
        this.suiteRepo = suiteRepo;
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
        this.tenantRepo = tenantRepo;
        this.stepExecutor = stepExecutor;
        this.metrics = metrics;
    }

    public void delete(UUID id) {
        execStepRepo.deleteAll(execStepRepo.findByExecutionIdOrderById(id));
        execRepo.deleteById(id);
    }

    public Long getTenantIdForExecution(UUID executionId) {
        ExecutionEntity e = execRepo.findById(executionId).orElse(null);
        if (e == null)
            return null;

        com.testnext.model.TestEntity t = testRepo.findById(e.testId).orElse(null);
        if (t == null)
            return null;

        com.testnext.model.TestSuiteEntity s = suiteRepo.findById(t.suiteId).orElse(null);
        if (s == null)
            return null;

        return projectRepo.findById(s.projectId).map(p -> p.getTenantId()).orElse(null);
    }

    public List<ExecutionDto> startSuite(Long suiteId) {
        List<com.testnext.model.TestEntity> tests = testRepo.findBySuiteId(suiteId);
        List<ExecutionDto> results = new java.util.ArrayList<>();
        for (com.testnext.model.TestEntity test : tests) {
            results.add(start(test.id, null));
        }
        return results;
    }

    public ExecutionDto start(Long testId, Long runFromStepId) {
        metrics.recordExecutionStarted();
        final Timer.Sample sample = metrics.recordExecutionStart();

        // create execution record
        ExecutionEntity e = new ExecutionEntity();
        e.id = UUID.randomUUID();
        e.testId = testId;
        e.status = "ACCEPTED";
        e.startedAt = Instant.now();
        e = execRepo.save(e);

        // load test steps
        List<TestStepEntity> steps = testStepRepo.findByTestIdOrderById(testId);
        for (TestStepEntity ts : steps) {
            ExecutionStepEntity ese = new ExecutionStepEntity();
            ese.id = UUID.randomUUID();
            ese.executionId = e.id;
            ese.stepDefinitionId = ts.stepDefinitionId;
            ese.parametersJson = ts.parametersJson;
            ese.status = "PENDING";
            ese.resultJson = null;
            execStepRepo.save(ese);
        }

        // dispatch async worker
        UUID executionId = e.id;
        executor.submit(() -> runExecution(executionId, sample));

        return toDto(e, false);
    }

    private void runExecution(UUID executionId, Timer.Sample sample) {
        try {
            List<ExecutionStepEntity> steps = execStepRepo.findByExecutionIdOrderById(executionId);
            ExecutionEntity e = execRepo.findById(executionId).orElseThrow();
            e.status = "RUNNING";
            execRepo.save(e);

            for (ExecutionStepEntity s : steps) {
                s.status = "RUNNING";
                s.startedAt = Instant.now();
                execStepRepo.save(s);

                try {
                    // Fetch step definition to get name
                    com.testnext.model.StepDefinitionEntity def = stepDefRepo.findById(s.stepDefinitionId)
                            .orElseThrow();

                    // Parse parameters
                    Map<String, Object> params = s.parametersJson != null ? mapper.readValue(s.parametersJson,
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                            }) : Map.of();

                    // Execute
                    com.testnext.execution.StepResult result = stepExecutor.execute(def.name, params);

                    s.status = result.isSuccess() ? "FINISHED" : "FAILED";
                    s.resultJson = mapper.writeValueAsString(
                            result.getOutput() != null ? result.getOutput()
                                    : Map.of("error", result.getErrorMessage()));
                } catch (Exception ex) {
                    s.status = "FAILED";
                    s.resultJson = "{\"error\": \"" + ex.getMessage() + "\"}";
                }

                s.finishedAt = Instant.now();
                execStepRepo.save(s);

                if ("FAILED".equals(s.status)) {
                    e.status = "FAILED";
                    break;
                }
            }

            if (!"FAILED".equals(e.status)) {
                e.status = "FINISHED";
            }
            e.finishedAt = Instant.now();
            execRepo.save(e);
            metrics.recordExecutionCompleted();
        } catch (Exception ex) {
            metrics.recordExecutionFailed();
            throw new RuntimeException(ex);
        } finally {
            metrics.recordExecutionStop(sample);
        }
    }

    public List<ExecutionDto> list(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            return java.util.Collections.emptyList();

        // Admin sees all
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
        if (isAdmin) {
            return execRepo.findAll().stream()
                    .map(e -> toDto(e, false))
                    .collect(Collectors.toList());
        }

        // Others see only their tenant's executions
        String username = auth.getName();
        java.util.Optional<com.testnext.user.SystemUser> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty())
            return java.util.Collections.emptyList();

        com.testnext.user.SystemUser user = userOpt.get();
        // If Test Manager, find their tenant
        if ("TEST_MANAGER".equals(user.getRole()) || "ROLE_TEST_MANAGER".equals(user.getRole())) {
            java.util.Optional<com.testnext.tenant.entity.TenantEntity> tenantOpt = tenantRepo
                    .findByTestManagerId(user.getId());
            if (tenantOpt.isPresent()) {
                return execRepo.findByTenantId(tenantOpt.get().getId()).stream()
                        .map(e -> toDto(e, false))
                        .collect(Collectors.toList());
            }
        }

        return java.util.Collections.emptyList();
    }

    public ExecutionDto get(UUID id) {
        ExecutionEntity e = execRepo.findById(id).orElse(null);
        if (e == null)
            return null;
        return toDto(e, true);
    }

    public List<ExecutionDto> listByProject(Long projectId) {
        return execRepo.findByProjectId(projectId).stream()
                .map(e -> toDto(e, false))
                .collect(Collectors.toList());
    }

    private ExecutionDto toDto(ExecutionEntity e, boolean includeSteps) {
        ExecutionDto out = new ExecutionDto();
        out.id = e.id;
        out.testId = e.testId;
        out.status = e.status;
        out.startedAt = e.startedAt;
        out.finishedAt = e.finishedAt;

        // Populate names
        testRepo.findById(e.testId).ifPresent(t -> {
            out.testName = t.name;
            suiteRepo.findById(t.suiteId).ifPresent(s -> out.suiteName = s.name);
        });

        if (includeSteps) {
            out.steps = execStepRepo.findByExecutionIdOrderById(e.id).stream().map(s -> {
                ExecutionDto.ExecutionStepDto es = new ExecutionDto.ExecutionStepDto();
                es.id = s.id;
                es.stepDefinitionId = s.stepDefinitionId;
                es.status = s.status;
                Map<String, Object> res = new java.util.HashMap<>();
                res.put("raw", s.resultJson);
                es.result = res;
                return es;
            }).collect(Collectors.toList());
        }
        return out;
    }
}
