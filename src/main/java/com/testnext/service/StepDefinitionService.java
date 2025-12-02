package com.testnext.service;

import com.testnext.api.dto.StepDefinitionDto;
import com.testnext.model.StepDefinitionEntity;
import com.testnext.repository.StepDefinitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StepDefinitionService {
    private final StepDefinitionRepository repo;
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public StepDefinitionService(StepDefinitionRepository repo) {
        this.repo = repo;
    }

    public StepDefinitionDto create(StepDefinitionDto in) {
        StepDefinitionEntity e = new StepDefinitionEntity();
        e.name = in.name;
        e.definition = in.description;
        try {
            e.inputsJson = in.inputs != null ? mapper.writeValueAsString(in.inputs) : null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        e = repo.save(e);
        return toDto(e);
    }

    public List<StepDefinitionDto> list() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private StepDefinitionDto toDto(StepDefinitionEntity e) {
        StepDefinitionDto d = new StepDefinitionDto();
        d.id = e.id;
        d.name = e.name;
        d.description = e.definition;
        try {
            if (e.inputsJson != null) {
                d.inputs = mapper.readValue(e.inputsJson,
                        new com.fasterxml.jackson.core.type.TypeReference<List<java.util.Map<String, Object>>>() {
                        });
            }
        } catch (Exception ex) {
            /* ignore or log */ }
        return d;
    }
}
