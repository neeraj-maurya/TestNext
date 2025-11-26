package com.testnext.migration;

import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Component
public class FlywayTenantMigrator {
    private final DataSource dataSource;

    public FlywayTenantMigrator(DataSource dataSource) { this.dataSource = dataSource; }

    /**
     * Run Flyway migrations for the given tenant schema. Migrations should be written to respect placeholders.
     */
    public void migrate(String schema) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .placeholders(Map.of("tenant_schema", schema))
                .load();
        flyway.migrate();
    }
}
