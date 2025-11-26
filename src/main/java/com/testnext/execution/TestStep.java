package com.testnext.execution;

import java.util.Map;

public class TestStep {
    private String id;
    private String stepDefinitionId;
    private String executorName; // name mapped to StepExecutor
    private Map<String, Object> parameters;
    private boolean dependent;
    private String dependsOnKey; // key in context to check
    private boolean blocking = true; // if false continue on failure
    private int maxAttempts = 1;
    private long retryDelayMs = 1000;
    private String outputKey; // where to put outputs in context

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStepDefinitionId() { return stepDefinitionId; }
    public void setStepDefinitionId(String stepDefinitionId) { this.stepDefinitionId = stepDefinitionId; }
    public String getExecutorName() { return executorName; }
    public void setExecutorName(String executorName) { this.executorName = executorName; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    public boolean isDependent() { return dependent; }
    public void setDependent(boolean dependent) { this.dependent = dependent; }
    public String getDependsOnKey() { return dependsOnKey; }
    public void setDependsOnKey(String dependsOnKey) { this.dependsOnKey = dependsOnKey; }
    public boolean isBlocking() { return blocking; }
    public void setBlocking(boolean blocking) { this.blocking = blocking; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(long retryDelayMs) { this.retryDelayMs = retryDelayMs; }
    public String getOutputKey() { return outputKey; }
    public void setOutputKey(String outputKey) { this.outputKey = outputKey; }
}
