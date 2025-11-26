package com.testnext.service;

import com.testnext.api.dto.ExecutionDto;
import com.testnext.api.dto.TestDto;
import com.testnext.model.ExecutionEntity;
import com.testnext.model.ExecutionStepEntity;
import com.testnext.model.TestStepEntity;
import com.testnext.observability.ObservabilityConfig;
import com.testnext.repository.ExecutionRepository;
import com.testnext.repository.ExecutionStepRepository;
import com.testnext.repository.TestRepository;
import com.testnext.repository.TestStepRepository;
import io.micrometer.core.instrument.Timer;
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
    private final TestRepository testRepo;
    private final TestStepRepository testStepRepo;
    private final ExecutionRepository execRepo;
    private final ExecutionStepRepository execStepRepo;
    private final ObservabilityConfig.ExecutionMetrics metrics;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ExecutionService(
            TestRepository testRepo,
            TestStepRepository testStepRepo,
            ExecutionRepository execRepo,
            ExecutionStepRepository execStepRepo,
            ObservabilityConfig.ExecutionMetrics metrics) {
        this.testRepo = testRepo;
        this.testStepRepo = testStepRepo;
        this.execRepo = execRepo;
        this.execStepRepo = execStepRepo;
        this.metrics = metrics;
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
            ese.status = "PENDING";
            ese.resultJson = null;
            execStepRepo.save(ese);
        }

        // dispatch async worker (simple mock runner)
        UUID executionId = e.id;
        executor.submit(() -> runExecution(executionId, sample));

        ExecutionDto out = new ExecutionDto();
        out.id = e.id;
        out.testId = e.testId;
        out.status = e.status;
        out.startedAt = e.startedAt;
        return out;
    }

    private void runExecution(UUID executionId, Timer.Sample sample) {
        try {
            // simple mock runner that updates step rows
            List<ExecutionStepEntity> steps = execStepRepo.findByExecutionIdOrderById(executionId);
            ExecutionEntity e = execRepo.findById(executionId).orElseThrow();
            e.status = "RUNNING";
            execRepo.save(e);

            for (ExecutionStepEntity s : steps) {
                s.status = "RUNNING";
                execStepRepo.save(s);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
                s.status = "FINISHED";
                s.resultJson = "{\"ok\":true}";
                execStepRepo.save(s);
            }

            e.status = "FINISHED";
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

    public ExecutionDto get(UUID id) {
        ExecutionEntity e = execRepo.findById(id).orElse(null);
        if (e == null) return null;
        ExecutionDto out = new ExecutionDto();
        out.id = e.id;
        out.testId = e.testId;
        out.status = e.status;
        out.startedAt = e.startedAt;
        out.finishedAt = e.finishedAt;
        out.steps = execStepRepo.findByExecutionIdOrderById(id).stream().map(s -> {
            ExecutionDto.ExecutionStepDto es = new ExecutionDto.ExecutionStepDto();
            es.id = s.id;
            es.stepDefinitionId = s.stepDefinitionId;
            es.status = s.status;
            es.result = Map.of("raw", s.resultJson);
            return es;
        }).collect(Collectors.toList());
        return out;
    }
}
