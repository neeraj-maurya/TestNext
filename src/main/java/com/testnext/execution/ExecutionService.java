package com.testnext.execution;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Future;

// Legacy execution service kept as a placeholder to avoid changing older package
// references. This class is intentionally NOT a Spring bean; the active
// application service lives in `com.testnext.service.ExecutionService`.
public class ExecutionService {
    private final ExecutionEngine engine;
    private final ExecutionRepositoryI repo;
    private final com.testnext.queue.JobQueue jobQueue;

    public ExecutionService(ExecutionEngine engine, ExecutionRepositoryI repo, com.testnext.queue.JobQueue jobQueue) {
        this.engine = engine;
        this.repo = repo;
        this.jobQueue = jobQueue;
    }

    public Future<ExecutionResult> startExecution(TestPlan plan) {
        // create DB execution row
        UUID execId = plan.getExecutionId();
        repo.createExecution(execId, null, "queued");
        return engine.runTest(plan);
    }

    public List<java.util.Map<String,Object>> getExecutionSteps(UUID executionId) {
        return repo.getExecutionSteps(executionId);
    }

    public java.util.List<java.util.UUID> getFailedStepExecIds(UUID executionId) {
        return repo.getFailedStepExecIds(executionId);
    }

    public RerunResult rerunFailedSteps(UUID executionId, java.util.List<java.util.UUID> failedStepExecIds, TestStepRepository stepRepo) {
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
        for (TestStep t : failed) { if (seen.add(t.getId())) merged.add(t); }
        for (TestStep t : dependents) { if (seen.add(t.getId())) merged.add(t); }

        List<TestStep> steps = merged;
        TestPlan plan = new TestPlan(steps);
        UUID newExecId = plan.getExecutionId();
        repo.createExecution(newExecId, null, "queued");
        // enqueue for asynchronous, non-blocking execution - return the Future from the queue
        java.util.concurrent.Future<ExecutionResult> fut = jobQueue.submit(plan);
        return new RerunResult(newExecId, fut);
    }
}
