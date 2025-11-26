package com.testnext.execution;

import java.util.Map;

/**
 * Interface for executing a single test step.
 * Implementations receive a stepDefinitionId and parameters and must return a StepResult.
 */
public interface StepExecutor {
    StepResult execute(String stepDefinitionId, Map<String, Object> parameters) throws Exception;
}
