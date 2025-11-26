package com.testnext.execution;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ExecutionRepositoryI {
    void createExecution(UUID executionId, Long testId, String status);
    void updateExecutionStatus(UUID executionId, String status);
    void createExecutionStep(UUID id, UUID executionId, Long testStepId, String status);
    void updateExecutionStepResult(UUID id, String status, String resultJson, int attempts);
    List<UUID> getFailedStepExecIds(UUID executionId);
    List<Map<String, Object>> getExecutionSteps(UUID executionId);
}
