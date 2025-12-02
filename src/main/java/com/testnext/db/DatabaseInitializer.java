package com.testnext.db;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures core database tables exist on startup (dev convenience).
 */
@Component
public class DatabaseInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbc;

    public DatabaseInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("DatabaseInitializer: ensuring core tables exist...");
        try {
            createSystemUsersTable();
            createRolesTable();
            createStepDefinitionsTable();
            createTenantsTable();
            createProjectsTable();
            createTestSuitesTable();
            createTestsTable();
            createTestStepsTable();
            createExecutionsTable();
            createExecutionStepsTable();
            seedInitialData();
            log.info("DatabaseInitializer: all tables verified/created successfully");
        } catch (Exception e) {
            log.warn("DatabaseInitializer: error during initialization", e);
        }
    }

    private void createSystemUsersTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS system_users (" +
                    "id VARCHAR(36) PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL UNIQUE, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "hashed_password VARCHAR(255), " +
                    "display_name VARCHAR(255), " +
                    "role VARCHAR(50) NOT NULL DEFAULT 'SYSTEM_ADMIN', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            log.debug("system_users table verified");
        } catch (Exception e) {
            log.debug("system_users table already exists or error: {}", e.getMessage());
        }
    }

    private void createRolesTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS roles (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100) NOT NULL UNIQUE, " +
                    "description VARCHAR(500), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
            log.debug("roles table verified");
        } catch (Exception e) {
            log.debug("roles table already exists or error: {}", e.getMessage());
        }
    }

    private void createStepDefinitionsTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS step_definitions (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "definition CLOB, " +
                    "inputs_json CLOB, " +
                    "created_at TIMESTAMP" +
                    ")");

            // Ensure column exists for existing tables
            try {
                jdbc.execute("ALTER TABLE step_definitions ADD COLUMN IF NOT EXISTS inputs_json CLOB");
            } catch (Exception e) {
                // ignore
            }

            log.debug("step_definitions table verified");
        } catch (Exception e) {
            log.debug("step_definitions table already exists or error: {}", e.getMessage());
        }
    }

    private void createTenantsTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS tenants (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "schema_name VARCHAR(255) NOT NULL UNIQUE, " +
                    "test_manager_id VARCHAR(36)" +
                    ")");
            log.debug("tenants table verified");
        } catch (Exception e) {
            log.debug("tenants table already exists or error: {}", e.getMessage());
        }
    }

    private void createProjectsTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS projects (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "tenant_id BIGINT NOT NULL, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "description TEXT" +
                    ")");
            log.debug("projects table verified");
        } catch (Exception e) {
            log.debug("projects table already exists or error: {}", e.getMessage());
        }
    }

    private void createTestSuitesTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS test_suites (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "project_id BIGINT NOT NULL, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "description VARCHAR(1000)" +
                    ")");
            log.debug("test_suites table verified");
        } catch (Exception e) {
            log.debug("test_suites table already exists or error: {}", e.getMessage());
        }
    }

    private void createTestsTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS tests (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "suite_id BIGINT NOT NULL, " +
                    "name VARCHAR(255) NOT NULL" +
                    ")");
            log.debug("tests table verified");
        } catch (Exception e) {
            log.debug("tests table already exists or error: {}", e.getMessage());
        }
    }

    private void createTestStepsTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS test_steps (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "test_id BIGINT NOT NULL, " +
                    "step_definition_id BIGINT NOT NULL, " +
                    "parameters_json CLOB" +
                    ")");
            log.debug("test_steps table verified");
        } catch (Exception e) {
            log.debug("test_steps table already exists or error: {}", e.getMessage());
        }
    }

    private void createExecutionsTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS executions (" +
                    "id UUID PRIMARY KEY, " +
                    "test_id BIGINT NOT NULL, " +
                    "status VARCHAR(50) NOT NULL, " +
                    "started_at TIMESTAMP, " +
                    "finished_at TIMESTAMP" +
                    ")");
            log.debug("executions table verified");
        } catch (Exception e) {
            log.debug("executions table already exists or error: {}", e.getMessage());
        }
    }

    private void createExecutionStepsTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS execution_steps (" +
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
            log.debug("execution_steps table verified");
        } catch (Exception e) {
            log.debug("execution_steps table already exists or error: {}", e.getMessage());
        }
    }

    private void seedInitialData() {
        try {
            // Seed dev users if they don't exist
            jdbc.execute("MERGE INTO system_users (id, username, email, hashed_password, display_name, role) " +
                    "KEY(username) VALUES (RANDOM_UUID(), 'admin', 'admin@local', 'admin', 'Administrator', 'SYSTEM_ADMIN')");
            jdbc.execute("MERGE INTO system_users (id, username, email, hashed_password, display_name, role) " +
                    "KEY(username) VALUES (RANDOM_UUID(), 'user', 'user@local', 'user', 'Local User', 'USER')");

            // Seed roles
            jdbc.execute("MERGE INTO roles (name, description) KEY(name) " +
                    "VALUES ('SYSTEM_ADMIN', 'System administrator with full access')");
            jdbc.execute("MERGE INTO roles (name, description) KEY(name) " +
                    "VALUES ('TEST_MANAGER', 'Can create and manage test suites and cases')");
            jdbc.execute("MERGE INTO roles (name, description) KEY(name) " +
                    "VALUES ('TEST_EXECUTOR', 'Can execute tests and view results')");

            // Seed step definitions
            jdbc.execute("MERGE INTO step_definitions (name, definition) KEY(name) " +
                    "VALUES ('HTTP GET Request', 'Send a GET request to the specified URL and verify response')");
            jdbc.execute("MERGE INTO step_definitions (name, definition) KEY(name) " +
                    "VALUES ('HTTP POST Request', 'Send a POST request with JSON payload')");
            jdbc.execute("MERGE INTO step_definitions (name, definition) KEY(name) " +
                    "VALUES ('Assert Response', 'Assert that response matches expectations')");

            // Seed tenants and projects if they don't exist
            if (jdbc.queryForObject("SELECT COUNT(*) FROM tenants", Integer.class) == 0) {
                jdbc.execute("INSERT INTO tenants (name, schema_name) VALUES ('TechCorp', 'tech_corp_schema')");
                jdbc.execute("INSERT INTO tenants (name, schema_name) VALUES ('FinTrade Inc', 'fintrade_schema')");

                jdbc.execute("INSERT INTO projects (tenant_id, name, description) VALUES " +
                        "(1, 'Trading Platform Tests', 'Test suite for trading platform features')");
                jdbc.execute("INSERT INTO projects (tenant_id, name, description) VALUES " +
                        "(1, 'Mobile App Tests', 'Test suite for mobile application')");
                jdbc.execute("INSERT INTO projects (tenant_id, name, description) VALUES " +
                        "(2, 'Core Banking Tests', 'Test suite for core banking operations')");
                jdbc.execute("INSERT INTO projects (tenant_id, name, description) VALUES " +
                        "(2, 'Risk Management Tests', 'Test suite for risk management module')");
            }

            // Seed test suites if they don't exist
            if (jdbc.queryForObject("SELECT COUNT(*) FROM test_suites", Integer.class) == 0) {
                jdbc.execute("INSERT INTO test_suites (project_id, name, description) VALUES " +
                        "(1, 'Authentication Tests', 'Test suite for trading platform authentication')");
                jdbc.execute("INSERT INTO test_suites (project_id, name, description) VALUES " +
                        "(1, 'Trading Operations', 'Test suite for core trading operations')");
                jdbc.execute("INSERT INTO test_suites (project_id, name, description) VALUES " +
                        "(2, 'Mobile Login Flow', 'Test suite for mobile app login')");
                jdbc.execute("INSERT INTO test_suites (project_id, name, description) VALUES " +
                        "(3, 'Account Opening', 'Test suite for new account creation')");
                jdbc.execute("INSERT INTO test_suites (project_id, name, description) VALUES " +
                        "(4, 'Compliance Checks', 'Test suite for regulatory compliance')");
            }

            log.info("DatabaseInitializer: initial data seeded successfully");
        } catch (Exception e) {
            log.debug("DatabaseInitializer: seed data may already exist or error during seed: {}", e.getMessage());
        }
    }
}
