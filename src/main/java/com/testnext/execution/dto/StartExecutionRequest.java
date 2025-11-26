package com.testnext.execution.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class StartExecutionRequest {
    @NotNull
    private String testId;

    // Optional: allow passing a subset of steps or parameters
    private List<String> stepIds;

    public String getTestId() { return testId; }
    public void setTestId(String testId) { this.testId = testId; }
    public List<String> getStepIds() { return stepIds; }
    public void setStepIds(List<String> stepIds) { this.stepIds = stepIds; }
}
