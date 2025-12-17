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
        e.description = in.description;
        // Generate a random refId if manual creation (legacy support)
        e.refId = in.refId != null ? in.refId : java.util.UUID.randomUUID().toString();
        try {
            e.inputsJson = in.inputs != null ? mapper.writeValueAsString(in.inputs) : null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        e = repo.save(e);
        return toDto(e);
    }

    @org.springframework.transaction.annotation.Transactional
    public void sync(StepDefinitionDto in) {
        StepDefinitionEntity e = repo.findByRefId(in.refId).orElse(new StepDefinitionEntity());
        e.refId = in.refId;
        e.name = in.name;
        e.description = in.description;
        e.returnType = in.returnType;
        e.parameterTypes = in.parameterTypes;

        try {
            e.inputsJson = in.inputs != null ? mapper.writeValueAsString(in.inputs) : null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        repo.save(e);
    }

    public List<StepDefinitionDto> list() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    private StepDefinitionDto toDto(StepDefinitionEntity e) {
        StepDefinitionDto d = new StepDefinitionDto();
        d.id = e.id;
        d.refId = e.refId;
        d.name = e.name;
        d.description = e.description;
        d.returnType = e.returnType;
        d.parameterTypes = e.parameterTypes;
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
