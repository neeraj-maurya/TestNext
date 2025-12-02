package com.testnext.execution;

import com.testnext.model.ExecutionEntity;
import com.testnext.model.ExecutionStepEntity;
import com.testnext.repository.ExecutionRepository;
import com.testnext.repository.ExecutionStepRepository;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

// Legacy execution service kept as a placeholder to avoid changing older package
// references. This class is intentionally NOT a Spring bean; the active
// application service lives in `com.testnext.service.ExecutionService`.
public class ExecutionService {
    private final ExecutionEngine engine;
    private final ExecutionRepository executionRepo;
    private final ExecutionStepRepository stepRepo;
    private final com.testnext.queue.JobQueue jobQueue;

    public ExecutionService(ExecutionEngine engine, ExecutionRepository executionRepo, ExecutionStepRepository stepRepo,
            com.testnext.queue.JobQueue jobQueue) {
        this.engine = engine;
        this.executionRepo = executionRepo;
        this.stepRepo = stepRepo;
        this.jobQueue = jobQueue;
    }

    public Future<ExecutionResult> startExecution(TestPlan plan) {
        // create DB execution row
        UUID execId = plan.getExecutionId();
        ExecutionEntity e = new ExecutionEntity();
        e.id = execId;
        e.testId = null;
        e.status = "queued";
        e.startedAt = Instant.now();
        executionRepo.save(e);

        return engine.runTest(plan);
    }

    public List<Map<String, Object>> getExecutionSteps(UUID executionId) {
        List<ExecutionStepEntity> steps = stepRepo.findByExecutionIdOrderByStartedAt(executionId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (ExecutionStepEntity r : steps) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.id);
            m.put("test_step_id", r.stepDefinitionId);
            m.put("status", r.status);
            m.put("result_json", r.resultJson);
            out.add(m);
        }
        return out;
    }

    public List<UUID> getFailedStepExecIds(UUID executionId) {
        List<ExecutionStepEntity> steps = stepRepo.findByExecutionIdAndStatus(executionId, "failed");
        return steps.stream().map(s -> s.id).collect(Collectors.toList());
    }

    public RerunResult rerunFailedSteps(UUID executionId, List<UUID> failedStepExecIds, TestStepRepository stepRepo) {
        // Reconstruct TestPlan from execution_step -> test_step mapping
        List<TestStep> failed = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();
        for (UUID execStepId : failedStepExecIds) {
            TestStep ts = stepRepo.findStepByExecutionStepId(execStepId);
            failed.add(ts);
            failedIds.add(ts.getId());
        }
        // find dependent steps recursively
        List<TestStep> dependents = stepRepo.findDependentStepsRecursively(failedIds);

        // merge preserving failed first, then dependents, de-duplicated
        List<TestStep> merged = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (TestStep t : failed) {
            if (seen.add(t.getId()))
                merged.add(t);
        }
        for (TestStep t : dependents) {
            if (seen.add(t.getId()))
                merged.add(t);
        }

        List<TestStep> steps = merged;
        TestPlan plan = new TestPlan(steps);
        UUID newExecId = plan.getExecutionId();

        ExecutionEntity e = new ExecutionEntity();
        e.id = newExecId;
        e.testId = null;
        e.status = "queued";
        e.startedAt = Instant.now();
        executionRepo.save(e);

        // enqueue for asynchronous, non-blocking execution - return the Future from the
        // queue
        java.util.concurrent.Future<ExecutionResult> fut = jobQueue.submit(plan);
        return new RerunResult(newExecId, fut);
    }
}
