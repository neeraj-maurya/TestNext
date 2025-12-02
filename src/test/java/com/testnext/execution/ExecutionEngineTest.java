package com.testnext.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testnext.model.ExecutionEntity;
import com.testnext.model.ExecutionStepEntity;
import com.testnext.repository.ExecutionRepository;
import com.testnext.repository.ExecutionStepRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExecutionEngineTest {
    @Test
    public void runSimplePlan() throws Exception {
        StepExecutorRegistry registry = new StepExecutorRegistry();
        registry.register("noop", (id, p) -> new StepResult(true, Map.of("ok", true), null));

        ExecutionRepository executionRepo = mock(ExecutionRepository.class);
        ExecutionStepRepository stepRepo = mock(ExecutionStepRepository.class);

        // Mock save methods to return the entity (or just do nothing if void, but save
        // returns entity)
        when(executionRepo.save(any(ExecutionEntity.class))).thenAnswer(i -> i.getArguments()[0]);
        when(stepRepo.save(any(ExecutionStepEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        ObjectMapper objectMapper = new ObjectMapper();
        ExecutionEngine engine = new ExecutionEngine(2, registry, executionRepo, stepRepo, objectMapper);

        TestStep s1 = new TestStep();
        s1.setId("s1");
        s1.setExecutorName("noop");
        s1.setStepDefinitionId("1");
        TestStep s2 = new TestStep();
        s2.setId("s2");
        s2.setExecutorName("noop");
        s2.setStepDefinitionId("2");

        TestPlan plan = new TestPlan(List.of(s1, s2));
        Future<ExecutionResult> f = engine.runTest(plan);
        ExecutionResult res = f.get();
        assertNotNull(res);
        assertEquals(2, res.getStepResults().size());
        engine.shutdown();
    }
}
