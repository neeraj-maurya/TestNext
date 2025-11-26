package com.testnext.api.dto;

import java.util.Map;

public class StepDefinitionDto {
    public Long id;
    public String name;
    public String description;
    public Map<String, Object> inputSchema;
    public Map<String, Object> outputSchema;

    public StepDefinitionDto() {}
}
