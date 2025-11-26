package com.testnext.execution;

import java.util.UUID;
import java.util.concurrent.Future;

public class RerunResult {
    private final UUID executionId;
    private final Future<ExecutionResult> future;

    public RerunResult(UUID executionId, Future<ExecutionResult> future) {
        this.executionId = executionId;
        this.future = future;
    }

    public UUID getExecutionId() { return executionId; }
    public Future<ExecutionResult> getFuture() { return future; }
}
