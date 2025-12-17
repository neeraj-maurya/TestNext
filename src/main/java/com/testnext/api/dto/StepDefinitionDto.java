package com.testnext.api.dto;

import java.util.Map;

public class StepDefinitionDto {
    public Long id;
    public String refId;
    public String name;
    public String description;
    public java.util.List<java.util.Map<String, Object>> inputs;
    public String returnType;
    public String parameterTypes;

    public StepDefinitionDto() {
    }
}
