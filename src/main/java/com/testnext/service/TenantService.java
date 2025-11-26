package com.testnext.service;

import com.testnext.api.dto.TenantDto;
import com.testnext.tenant.entity.TenantEntity;
import com.testnext.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TenantService {
    private final TenantRepository repo;

    public TenantService(TenantRepository repo) {
        this.repo = repo;
    }

    public TenantDto create(String name, String schemaName) {
        TenantEntity e = new TenantEntity();
        e.setName(name);
        e.setSchemaName(schemaName == null ? name.toLowerCase().replaceAll("[^a-z0-9_]+","_") : schemaName);
        TenantEntity saved = repo.save(e);
        return new TenantDto(saved.getId(), saved.getName(), saved.getSchemaName());
    }

    public List<TenantDto> list() {
        return repo.findAll().stream()
                .map(e -> new TenantDto(e.getId(), e.getName(), e.getSchemaName()))
                .collect(Collectors.toList());
    }
}
