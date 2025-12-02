package com.testnext.service;

import com.testnext.api.dto.TenantDto;
import com.testnext.tenant.entity.TenantEntity;
import com.testnext.repository.TenantRepository;
import com.testnext.db.SchemaInitializer;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TenantService {
    private final TenantRepository repo;
    private final SchemaInitializer schemaInitializer;

    public TenantService(TenantRepository repo, SchemaInitializer schemaInitializer) {
        this.repo = repo;
        this.schemaInitializer = schemaInitializer;
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public TenantDto create(String name, String schemaName, java.util.UUID testManagerId) {
        TenantEntity e = new TenantEntity();
        e.setName(name);
        String safeSchema = schemaName == null ? name.toLowerCase().replaceAll("[^a-z0-9_]+", "_") : schemaName;
        e.setSchemaName(safeSchema);
        e.setTestManagerId(testManagerId);

        TenantEntity saved = repo.save(e);

        // Initialize schema
        schemaInitializer.initialize(safeSchema);

        return new TenantDto(saved.getId(), saved.getName(), saved.getSchemaName(), saved.getTestManagerId());
    }

    public List<TenantDto> list() {
        return repo.findAll().stream()
                .map(e -> new TenantDto(e.getId(), e.getName(), e.getSchemaName(), e.getTestManagerId()))
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public TenantDto update(Long id, String name, String schemaName, java.util.UUID testManagerId) {
        var opt = repo.findById(id);
        if (opt.isEmpty())
            return null;
        TenantEntity e = opt.get();
        if (name != null)
            e.setName(name);
        if (schemaName != null)
            e.setSchemaName(schemaName);
        if (testManagerId != null)
            e.setTestManagerId(testManagerId);

        TenantEntity saved = repo.save(e);
        return new TenantDto(saved.getId(), saved.getName(), saved.getSchemaName(), saved.getTestManagerId());
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
