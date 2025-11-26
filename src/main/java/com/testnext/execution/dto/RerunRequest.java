package com.testnext.execution.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class RerunRequest {
    @NotEmpty
    private List<String> stepExecIds; // execution_step ids to rerun

    public List<String> getStepExecIds() { return stepExecIds; }
    public void setStepExecIds(List<String> stepExecIds) { this.stepExecIds = stepExecIds; }
}
