package com.testnext.execution;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testnext.model.ExecutionEntity;
import com.testnext.model.ExecutionStepEntity;
import com.testnext.repository.ExecutionRepository;
import com.testnext.repository.ExecutionStepRepository;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.*;

/**
 * Execution engine with basic DAG-aware parallelism and persistence hooks.
 */
public class ExecutionEngine {
    private final ExecutorService executor;
    private final StepExecutorRegistry registry;
    private final ExecutionRepository executionRepo;
    private final ExecutionStepRepository stepRepo;
    private final ObjectMapper objectMapper;

    public ExecutionEngine(int poolSize, StepExecutorRegistry registry, ExecutionRepository executionRepo,
            ExecutionStepRepository stepRepo, ObjectMapper objectMapper) {
        this.registry = registry;
        this.executionRepo = executionRepo;
        this.stepRepo = stepRepo;
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
        ExecutionEntity execEntity = new ExecutionEntity();
        execEntity.id = execId;
        execEntity.testId = null; // TestPlan doesn't seem to have testId easily available or it's null for now
        execEntity.status = "running";
        execEntity.startedAt = Instant.now();
        executionRepo.save(execEntity);

        // Track step states
        Map<String, StepResult> stepResults = new ConcurrentHashMap<>();
        Map<String, TestStep> remaining = new ConcurrentHashMap<>();
        for (TestStep s : plan.getSteps())
            remaining.put(s.getId(), s);

        CompletionService<StepExecutionOutcome> completionService = new ExecutorCompletionService<>(executor);
        Map<String, Future<StepExecutionOutcome>> running = new ConcurrentHashMap<>();

        // Helper to determine readiness
        while (!remaining.isEmpty() || !running.isEmpty()) {
            // submit ready tasks
            for (TestStep s : List.copyOf(remaining.values())) {
                boolean ready = true;
                if (s.isDependent() && s.getDependsOnKey() != null) {
                    // dependency satisfied if exists in any stepResults
                    if (!stepResults.keySet().contains(s.getDependsOnKey()))
                        ready = false;
                }
                if (ready) {
                    // register execution step in DB
                    UUID stepExecId = UUID.randomUUID();
                    ExecutionStepEntity stepEntity = new ExecutionStepEntity();
                    stepEntity.id = stepExecId;
                    stepEntity.executionId = execId;
                    stepEntity.stepDefinitionId = Long.parseLong(s.getStepDefinitionId());
                    stepEntity.status = "queued";
                    stepEntity.startedAt = Instant.now();
                    stepRepo.save(stepEntity);

                    Future<StepExecutionOutcome> f = completionService.submit(() -> executeStep(s, stepExecId));
                    running.put(s.getId(), f);
                    remaining.remove(s.getId());
                }
            }

            // wait for next completed
            try {
                Future<StepExecutionOutcome> completed = completionService.poll(500, TimeUnit.MILLISECONDS);
                if (completed == null)
                    continue;
                StepExecutionOutcome outcome = completed.get();
                stepResults.put(outcome.stepId, outcome.result);

                // Update step result in DB
                Optional<ExecutionStepEntity> stepOpt = stepRepo.findById(outcome.stepExecId);
                if (stepOpt.isPresent()) {
                    ExecutionStepEntity s = stepOpt.get();
                    s.status = outcome.result.isSuccess() ? "success" : "failed";
                    s.attempts = outcome.attempts;
                    s.finishedAt = Instant.now();
                    try {
                        s.resultJson = objectMapper.writeValueAsString(
                                outcome.result.getOutput() == null ? Map.of("error", outcome.result.getErrorMessage())
                                        : Map.of("output", outcome.result.getOutput(), "error",
                                                outcome.result.getErrorMessage()));
                    } catch (Exception ex) {
                        s.resultJson = "{}";
                    }
                    stepRepo.save(s);
                }

                running.remove(outcome.stepId);
                // if blocking failure, abort
                if (!outcome.result.isSuccess()) {
                    // set execution failed and return
                    Optional<ExecutionEntity> eOpt = executionRepo.findById(execId);
                    if (eOpt.isPresent()) {
                        ExecutionEntity e = eOpt.get();
                        e.status = "failed";
                        e.finishedAt = Instant.now();
                        executionRepo.save(e);
                    }

                    result = new ExecutionResult(execId);
                    result.getStepResults().putAll(stepResults);
                    return result;
                }
            } catch (Exception e) {
                // log and continue
            }
        }

        Optional<ExecutionEntity> eOpt = executionRepo.findById(execId);
        if (eOpt.isPresent()) {
            ExecutionEntity e = eOpt.get();
            e.status = "success";
            e.finishedAt = Instant.now();
            executionRepo.save(e);
        }

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
                if (sr != null && sr.isSuccess())
                    break;
            } catch (Exception ex) {
                sr = new StepResult(false, null, ex.getMessage());
            }
            if (attempts < ts.getMaxAttempts()) {
                try {
                    Thread.sleep(ts.getRetryDelayMs());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        if (sr == null)
            sr = new StepResult(false, null, "no result");
        return new StepExecutionOutcome(ts.getId(), stepExecId, sr, attempts);
    }

    public void shutdown() {
        executor.shutdown();
    }

    private static class StepExecutionOutcome {
        final String stepId;
        final UUID stepExecId;
        final StepResult result;
        final int attempts;

        StepExecutionOutcome(String stepId, UUID stepExecId, StepResult result, int attempts) {
            this.stepId = stepId;
            this.stepExecId = stepExecId;
            this.result = result;
            this.attempts = attempts;
        }
    }
}
