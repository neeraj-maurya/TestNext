package com.testnext.queue;

import com.testnext.execution.ExecutionEngine;
import com.testnext.execution.ExecutionResult;
import com.testnext.execution.TestPlan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InMemoryJobQueue implements JobQueue {
    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private final ExecutionEngine engine;

    public InMemoryJobQueue(ExecutionEngine engine) { this.engine = engine; }

    @Override
    public Future<ExecutionResult> submit(TestPlan plan) {
        // submit to the pool which will call the engine to run the plan
        return pool.submit(() -> engine.runTest(plan).get());
    }
}
