package com.testnext.provisioning;

import com.testnext.tenant.SchemaNameValidator;
import com.testnext.migration.FlywayTenantMigrator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class TenantMigrationRunner {
    private final DataSource dataSource; // central datasource used to execute schema-level commands
    private final JdbcTemplate jdbc;
    private final SchemaNameValidator schemaNameValidator;
    private final FlywayTenantMigrator flywayMigrator;

    public TenantMigrationRunner(DataSource dataSource, SchemaNameValidator schemaNameValidator, FlywayTenantMigrator flywayMigrator) {
        this.dataSource = dataSource;
        this.jdbc = new JdbcTemplate(dataSource);
        this.schemaNameValidator = schemaNameValidator;
        this.flywayMigrator = flywayMigrator;
    }

    public void createSchemaAndMigrate(String schema) {
        // Sanitize and validate schema name using the validator (throws IllegalArgumentException if invalid)
        String safe = schemaNameValidator.sanitize(schema);
        if (!schemaNameValidator.patternValid(safe)) {
            throw new IllegalArgumentException("Invalid schema name: " + safe);
        }
        // Optionally check registry presence; in provisioning flow the tenant is already inserted into admin_tenants
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS " + safe);
        // Run Flyway per-tenant migrations
        flywayMigrator.migrate(safe);
    }

    public void seedStepDefinitions(String schema) {
        String safe = schemaNameValidator.sanitize(schema);
        jdbc.execute("SET search_path TO " + safe + ",public");
        // Minimal seed - prefer Flyway callback or dedicated SQL script
        jdbc.update("INSERT INTO step_definitions (id, name, description, rest_endpoint, input_schema, output_schema, created_by) VALUES (gen_random_uuid(), ?, ?, ?, ?::jsonb, ?::jsonb, NULL)",
                "HTTP Request", "Execute HTTP request", "/exec/http-request",
                "{\"fields\": [{\"name\":\"method\",\"type\":\"string\",\"label\":\"Method\",\"enum\":[\"GET\",\"POST\"]},{\"name\":\"url\",\"type\":\"string\",\"label\":\"URL\"}]}",
                "{\"fields\": [{\"name\":\"status\",\"type\":\"integer\"},{\"name\":\"body\",\"type\":\"json\"}]}"
        );
        jdbc.execute("SET search_path TO public");
    }
}
