package com.testnext.execution;

import java.util.Map;

public class StepResult {
    private boolean success;
    private Map<String, Object> output;
    private String errorMessage;

    public StepResult() {}

    public StepResult(boolean success, Map<String, Object> output, String errorMessage) {
        this.success = success;
        this.output = output;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Map<String, Object> getOutput() { return output; }
    public void setOutput(Map<String, Object> output) { this.output = output; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
