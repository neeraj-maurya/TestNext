package com.testnext.api.dto;

import java.util.List;
import java.util.Map;

public class TestDto {
    public Long id;
    public Long suiteId;
    public String name;
    public List<TestStepDto> steps;

    public static class TestStepDto {
        public Long stepDefinitionId;
        public Map<String, Object> parameters;
    }
}
