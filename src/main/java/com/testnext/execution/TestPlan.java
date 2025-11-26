package com.testnext.execution;

import java.util.List;
import java.util.UUID;

public class TestPlan {
    private final UUID executionId = UUID.randomUUID();
    private final List<TestStep> steps;

    public TestPlan(List<TestStep> steps) { this.steps = steps; }

    public UUID getExecutionId() { return executionId; }
    public List<TestStep> getSteps() { return steps; }
}
