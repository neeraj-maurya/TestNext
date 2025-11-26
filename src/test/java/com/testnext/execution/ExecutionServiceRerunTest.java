package com.testnext.execution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ExecutionServiceRerunTest {

    private ExecutionEngine engine;
    private ExecutionRepository repo;
    private TestStepRepository stepRepo;
    private com.testnext.queue.JobQueue jobQueue;
    private ExecutionService service;

    @BeforeEach
    public void setup() {
        engine = mock(ExecutionEngine.class);
        repo = mock(ExecutionRepository.class);
        stepRepo = mock(TestStepRepository.class);
        jobQueue = mock(com.testnext.queue.JobQueue.class);
        service = new ExecutionService(engine, repo, jobQueue);
    }

    @Test
    public void rerunFailedSteps_mergesFailedAndDependentsAndSubmitsPlan() throws Exception {
        UUID execId = UUID.randomUUID();
        UUID failedExecStepId = UUID.randomUUID();

        // stub stepRepo to return a failed TestStep for the execution step id
        TestStep failed = new TestStep();
        failed.setId("step-1");
        failed.setStepDefinitionId("http");
        failed.setExecutorName("http");

        when(stepRepo.findStepByExecutionStepId(failedExecStepId)).thenReturn(failed);

        // dependent step
        TestStep dep = new TestStep();
        dep.setId("step-2");
        dep.setStepDefinitionId("assert");
        dep.setExecutorName("assert");

        when(stepRepo.findDependentStepsRecursively(List.of("step-1"))).thenReturn(List.of(dep));


    // mock jobQueue.submit to return a future
    @SuppressWarnings("unchecked")
    Future<ExecutionResult> fut = mock(Future.class);
    when(jobQueue.submit(any())).thenReturn(fut);

    // call service
    RerunResult result = service.rerunFailedSteps(execId, List.of(failedExecStepId), stepRepo);

    assertNotNull(result);
    assertEquals(result.getExecutionId().toString().length() > 0, true);

        // verify repo created a new execution
        verify(repo, times(1)).createExecution(any(UUID.class), isNull(), eq("queued"));

    // capture the plan submitted to jobQueue
    ArgumentCaptor<TestPlan> cap = ArgumentCaptor.forClass(TestPlan.class);
    verify(jobQueue, times(1)).submit(cap.capture());

    TestPlan submitted = cap.getValue();
        assertNotNull(submitted);
        assertEquals(2, submitted.getSteps().size());
        assertEquals("step-1", submitted.getSteps().get(0).getId());
        assertEquals("step-2", submitted.getSteps().get(1).getId());
    }
}
