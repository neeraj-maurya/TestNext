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

    public StepDefinitionService(StepDefinitionRepository repo) {
        this.repo = repo;
    }

    public StepDefinitionDto create(StepDefinitionDto in) {
        StepDefinitionEntity e = new StepDefinitionEntity();
        e.name = in.name;
        e.definition = in.description;
        e = repo.save(e);
        StepDefinitionDto out = new StepDefinitionDto();
        out.id = e.id; out.name = e.name; out.description = e.definition;
        return out;
    }

    public List<StepDefinitionDto> list() {
        return repo.findAll().stream().map(e -> {
            StepDefinitionDto d = new StepDefinitionDto(); d.id = e.id; d.name = e.name; d.description = e.definition; return d;
        }).collect(Collectors.toList());
    }
}
