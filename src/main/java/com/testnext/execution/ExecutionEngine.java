package com.testnext.execution;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.*;
import java.util.*;

/**
 * Execution engine with basic DAG-aware parallelism and persistence hooks.
 */
public class ExecutionEngine {
    private final ExecutorService executor;
    private final StepExecutorRegistry registry;
    private final ExecutionRepositoryI repo;
    private final ObjectMapper objectMapper;

    public ExecutionEngine(int poolSize, StepExecutorRegistry registry, ExecutionRepositoryI repo, ObjectMapper objectMapper) {
        this.registry = registry;
        this.repo = repo;
        this.objectMapper = objectMapper;
        this.executor = Executors.newFixedThreadPool(poolSize, new CustomizableThreadFactory("testnext-exec-"));
    }

    public Future<ExecutionResult> runTest(TestPlan plan) {
        return executor.submit(() -> executePlan(plan));
    }

    private ExecutionResult executePlan(TestPlan plan) {
        UUID execId = plan.getExecutionId();
        ExecutionResult result = new ExecutionResult(execId);

        // Persist execution as queued -> running
        repo.createExecution(execId, null, "running");

        // Track step states
        Map<String, StepResult> stepResults = new ConcurrentHashMap<>();
        Map<String, TestStep> remaining = new ConcurrentHashMap<>();
        for (TestStep s : plan.getSteps()) remaining.put(s.getId(), s);

        CompletionService<StepExecutionOutcome> completionService = new ExecutorCompletionService<>(executor);
        Map<String, Future<StepExecutionOutcome>> running = new ConcurrentHashMap<>();

        // Helper to determine readiness
        while (!remaining.isEmpty() || !running.isEmpty()) {
            // submit ready tasks
            for (TestStep s : List.copyOf(remaining.values())) {
                boolean ready = true;
                if (s.isDependent() && s.getDependsOnKey() != null) {
                    // dependency satisfied if exists in any stepResults
                    if (!stepResults.keySet().contains(s.getDependsOnKey())) ready = false;
                }
                if (ready) {
                    // register execution step in DB
                    UUID stepExecId = UUID.randomUUID();
                    repo.createExecutionStep(stepExecId, execId, null, "queued");
                    Future<StepExecutionOutcome> f = completionService.submit(() -> executeStep(s, stepExecId));
                    running.put(s.getId(), f);
                    remaining.remove(s.getId());
                }
            }

            // wait for next completed
            try {
                Future<StepExecutionOutcome> completed = completionService.poll(500, TimeUnit.MILLISECONDS);
                if (completed == null) continue;
                StepExecutionOutcome outcome = completed.get();
                stepResults.put(outcome.stepId, outcome.result);
                // serialize real result JSON and update DB with attempts
                try {
                    String resultJson = objectMapper.writeValueAsString(outcome.result.getOutput() == null ? Map.of("error", outcome.result.getErrorMessage()) : Map.of("output", outcome.result.getOutput(), "error", outcome.result.getErrorMessage()));
                    repo.updateExecutionStepResult(outcome.stepExecId, outcome.result.isSuccess() ? "success" : "failed", resultJson, outcome.attempts);
                } catch (Exception ex) {
                    // fallback to an empty json
                    repo.updateExecutionStepResult(outcome.stepExecId, outcome.result.isSuccess() ? "success" : "failed", "{}", outcome.attempts);
                }
                running.remove(outcome.stepId);
                // if blocking failure, abort
                if (!outcome.result.isSuccess()) {
                    // set execution failed and return
                    repo.updateExecutionStatus(execId, "failed");
                    result = new ExecutionResult(execId);
                    result.getStepResults().putAll(stepResults);
                    return result;
                }
            } catch (Exception e) {
                // log and continue
            }
        }

        repo.updateExecutionStatus(execId, "success");
        result.getStepResults().putAll(stepResults);
        return result;
    }

    private StepExecutionOutcome executeStep(TestStep ts, UUID stepExecId) {
        StepExecutor exec = registry.get(ts.getExecutorName());
        StepResult sr = null;
        int attempts = 0;
        while (attempts < Math.max(1, ts.getMaxAttempts())) {
            attempts++;
            try {
                sr = exec.execute(ts.getStepDefinitionId(), ts.getParameters());
                if (sr != null && sr.isSuccess()) break;
            } catch (Exception ex) {
                sr = new StepResult(false, null, ex.getMessage());
            }
            if (attempts < ts.getMaxAttempts()) {
                try { Thread.sleep(ts.getRetryDelayMs()); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
        }
        if (sr == null) sr = new StepResult(false, null, "no result");
        return new StepExecutionOutcome(ts.getId(), stepExecId, sr, attempts);
    }

    public void shutdown() { executor.shutdown(); }

    private static class StepExecutionOutcome {
        final String stepId;
        final UUID stepExecId;
        final StepResult result;
        final int attempts;

        StepExecutionOutcome(String stepId, UUID stepExecId, StepResult result, int attempts) {
            this.stepId = stepId; this.stepExecId = stepExecId; this.result = result; this.attempts = attempts;
        }
    }
}
