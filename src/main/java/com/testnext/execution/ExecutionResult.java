package com.testnext.execution;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExecutionResult {
    private UUID executionId;
    private Map<String, StepResult> stepResults = new HashMap<>();

    public ExecutionResult(UUID executionId) { this.executionId = executionId; }

    public void addStepResult(String stepId, StepResult sr) { stepResults.put(stepId, sr); }

    public Map<String, StepResult> getStepResults() { return stepResults; }
    public UUID getExecutionId() { return executionId; }
}
