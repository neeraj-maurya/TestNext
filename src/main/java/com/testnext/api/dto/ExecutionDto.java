package com.testnext.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExecutionDto {
    public UUID id;
    public Long testId;
    public String status;
    public Instant startedAt;
    public Instant finishedAt;
    public List<ExecutionStepDto> steps;

    public static class ExecutionStepDto {
        public UUID id;
        public Long stepDefinitionId;
        public String status;
        public Map<String, Object> result;
    }
}
