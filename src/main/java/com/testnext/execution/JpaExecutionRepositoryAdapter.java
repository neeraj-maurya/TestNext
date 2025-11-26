package com.testnext.execution;

import com.testnext.model.ExecutionEntity;
import com.testnext.model.ExecutionStepEntity;
import com.testnext.repository.ExecutionRepository;
import com.testnext.repository.ExecutionStepRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaExecutionRepositoryAdapter implements ExecutionRepositoryI {
    private final ExecutionRepository executionRepo;
    private final ExecutionStepRepository stepRepo;

    public JpaExecutionRepositoryAdapter(ExecutionRepository executionRepo, ExecutionStepRepository stepRepo) {
        this.executionRepo = executionRepo;
        this.stepRepo = stepRepo;
    }

    @Override
    public void createExecution(UUID executionId, Long testId, String status) {
        ExecutionEntity e = new ExecutionEntity();
        e.id = executionId;
        e.testId = testId;
        e.status = status;
        e.startedAt = Instant.now();
        executionRepo.save(e);
    }

    @Override
    public void updateExecutionStatus(UUID executionId, String status) {
        Optional<ExecutionEntity> oe = executionRepo.findById(executionId);
        if (oe.isPresent()) {
            ExecutionEntity e = oe.get();
            e.status = status;
            if ("success".equals(status) || "failed".equals(status)) {
                e.finishedAt = Instant.now();
            }
            executionRepo.save(e);
        }
    }

    @Override
    public void createExecutionStep(UUID id, UUID executionId, Long testStepId, String status) {
        ExecutionStepEntity s = new ExecutionStepEntity();
        s.id = id;
        s.executionId = executionId;
        s.stepDefinitionId = testStepId;
        s.status = status;
        s.startedAt = Instant.now();
        stepRepo.save(s);
    }

    @Override
    public void updateExecutionStepResult(UUID id, String status, String resultJson, int attempts) {
        Optional<ExecutionStepEntity> os = stepRepo.findById(id);
        if (os.isPresent()) {
            ExecutionStepEntity s = os.get();
            s.status = status;
            s.resultJson = resultJson;
            s.attempts = attempts;
            s.finishedAt = Instant.now();
            stepRepo.save(s);
        }
    }

    @Override
    public List<UUID> getFailedStepExecIds(UUID executionId) {
        List<ExecutionStepEntity> rows = stepRepo.findByExecutionIdAndStatus(executionId, "failed");
        List<UUID> out = new ArrayList<>();
        for (ExecutionStepEntity r : rows) out.add(r.id);
        return out;
    }

    @Override
    public List<Map<String, Object>> getExecutionSteps(UUID executionId) {
        List<ExecutionStepEntity> rows = stepRepo.findByExecutionIdOrderByStartedAt(executionId);
        List<Map<String,Object>> out = new ArrayList<>();
        for (ExecutionStepEntity r : rows) {
            Map<String,Object> m = new HashMap<>();
            m.put("id", r.id);
            m.put("test_step_id", r.stepDefinitionId);
            m.put("status", r.status);
            m.put("result_json", r.resultJson);
            out.add(m);
        }
        return out;
    }
}
