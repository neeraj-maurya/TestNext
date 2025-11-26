package com.testnext.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutionEngineTest {
    @Test
    public void runSimplePlan() throws Exception {
        StepExecutorRegistry registry = new StepExecutorRegistry();
        registry.register("noop", (id, p) -> new StepResult(true, Map.of("ok", true), null));

        // simple in-memory ExecutionRepositoryI for tests
        ExecutionRepositoryI repo = new ExecutionRepositoryI() {
            @Override public void createExecution(UUID executionId, Long testId, String status) {}
            @Override public void updateExecutionStatus(UUID executionId, String status) {}
            @Override public void createExecutionStep(UUID id, UUID executionId, Long testStepId, String status) {}
            @Override public void updateExecutionStepResult(UUID id, String status, String resultJson, int attempts) {}
            @Override public java.util.List<UUID> getFailedStepExecIds(UUID executionId) { return java.util.Collections.emptyList(); }
            @Override public java.util.List<java.util.Map<String, Object>> getExecutionSteps(UUID executionId) { return java.util.Collections.emptyList(); }
        };

        ObjectMapper objectMapper = new ObjectMapper();
        ExecutionEngine engine = new ExecutionEngine(2, registry, repo, objectMapper);

        TestStep s1 = new TestStep(); s1.setId("s1"); s1.setExecutorName("noop"); s1.setStepDefinitionId("sd1");
        TestStep s2 = new TestStep(); s2.setId("s2"); s2.setExecutorName("noop"); s2.setStepDefinitionId("sd2");

        TestPlan plan = new TestPlan(List.of(s1, s2));
        Future<ExecutionResult> f = engine.runTest(plan);
        ExecutionResult res = f.get();
        assertNotNull(res);
        assertEquals(2, res.getStepResults().size());
        engine.shutdown();
    }
}
