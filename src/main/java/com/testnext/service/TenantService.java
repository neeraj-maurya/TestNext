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

    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public TenantService(TenantRepository repo, SchemaInitializer schemaInitializer,
            org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.repo = repo;
        this.schemaInitializer = schemaInitializer;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public TenantDto create(String name, String schemaName, java.util.UUID testManagerId, boolean active) {
        TenantEntity e = new TenantEntity();
        e.setName(name);
        String safeSchema = schemaName == null ? name.toLowerCase().replaceAll("[^a-z0-9_]+", "_") : schemaName;
        e.setSchemaName(safeSchema);
        e.setTestManagerId(testManagerId);
        e.setActive(active);

        TenantEntity saved = repo.save(e);

        // Initialize schema
        schemaInitializer.initialize(safeSchema);

        return new TenantDto(saved.getId(), saved.getName(), saved.getSchemaName(), saved.getTestManagerId(),
                saved.isActive());
    }

    public List<TenantDto> list() {
        return repo.findAll().stream()
                .map(e -> new TenantDto(e.getId(), e.getName(), e.getSchemaName(), e.getTestManagerId(), e.isActive()))
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public TenantDto update(Long id, String name, String schemaName, java.util.UUID testManagerId, Boolean active) {
        var opt = repo.findById(id);
        if (opt.isEmpty())
            return null;
        TenantEntity e = opt.get();
        if (name != null)
            e.setName(name);
        if (schemaName != null)
            e.setSchemaName(schemaName);

        // Allow unsetting testManagerId (nullable)
        e.setTestManagerId(testManagerId);

        if (active != null)
            e.setActive(active);

        TenantEntity saved = repo.save(e);
        return new TenantDto(saved.getId(), saved.getName(), saved.getSchemaName(), saved.getTestManagerId(),
                saved.isActive());
    }

    @Transactional
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public void delete(Long id) {
        repo.findById(id).ifPresent(tenant -> {
            String schema = tenant.getSchemaName();
            repo.deleteById(id);
            dropSchema(schema);
        });
    }

    private void dropSchema(String schemaName) {
        if (schemaName != null && !schemaName.isBlank()) {
            // Validate schema name to prevent SQL injection (simple check)
            if (!schemaName.matches("^[a-z0-9_]+$")) {
                throw new IllegalArgumentException("Invalid schema name");
            }
            // Use String.format for logging/debugging, but strictly validated
            String sql = "DROP SCHEMA IF EXISTS \"" + schemaName + "\" CASCADE";
            System.out.println("TenantService: Dropping schema: " + schemaName);
            jdbcTemplate.execute(sql);
        }
    }
}
