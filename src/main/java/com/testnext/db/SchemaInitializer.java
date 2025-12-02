package com.testnext.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SchemaInitializer {
    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);
    private final JdbcTemplate jdbc;

    public SchemaInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void initialize(String schemaName) {
        log.info("Initializing schema: {}", schemaName);
        // Quote schema name to handle special characters (e.g. hyphens in UUIDs)
        String quotedSchema = "\"" + schemaName + "\"";

        try {
            // Create Schema
            jdbc.execute("CREATE SCHEMA IF NOT EXISTS " + quotedSchema);

            // Switch to new schema to create tables
            // Note: We use the fully qualified name or set search_path.
            // Setting search_path for the session might affect the connection pool if not
            // reset,
            // but here we are likely in a transaction or short-lived operation.
            // Safer to use schema.table syntax or SET search_path LOCAL if supported.
            // H2 supports SET SCHEMA.

            // We'll execute DDLs with schema prefix to be safe and explicit.

            createTestSuitesTable(quotedSchema);
            createTestsTable(quotedSchema);
            createTestStepsTable(quotedSchema);
            createExecutionsTable(quotedSchema);
            createExecutionStepsTable(quotedSchema);

            log.info("Schema {} initialized successfully", schemaName);
        } catch (Exception e) {
            log.error("Error initializing schema {}", schemaName, e);
            throw new RuntimeException("Failed to initialize tenant schema", e);
        }
    }

    private void createTestSuitesTable(String schema) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + schema + ".test_suites (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "project_id BIGINT NOT NULL, " +
                "name VARCHAR(255) NOT NULL, " +
                "description VARCHAR(1000)" +
                ")");
    }

    private void createTestsTable(String schema) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + schema + ".tests (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "suite_id BIGINT NOT NULL, " +
                "name VARCHAR(255) NOT NULL" +
                ")");
    }

    private void createTestStepsTable(String schema) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + schema + ".test_steps (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "test_id BIGINT NOT NULL, " +
                "step_definition_id BIGINT NOT NULL, " +
                "parameters_json CLOB" +
                ")");
    }

    private void createExecutionsTable(String schema) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + schema + ".executions (" +
                "id UUID PRIMARY KEY, " +
                "test_id BIGINT NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "started_at TIMESTAMP, " +
                "finished_at TIMESTAMP" +
                ")");
    }

    private void createExecutionStepsTable(String schema) {
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + schema + ".execution_steps (" +
                "id UUID PRIMARY KEY, " +
                "execution_id UUID NOT NULL, " +
                "step_definition_id BIGINT NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "result_json CLOB, " +
                "parameters_json CLOB, " +
                "started_at TIMESTAMP, " +
                "finished_at TIMESTAMP, " +
                "attempts INT" +
                ")");
    }
}
