-- Dev-only migration: recreate executions and execution_steps with UUID ids
-- This migration is added under db/migration/dev and only loaded by the 'dev' profile.
DROP TABLE IF EXISTS execution_steps;
DROP TABLE IF EXISTS executions;

CREATE TABLE executions (
  id VARCHAR(36) PRIMARY KEY,
  test_id BIGINT,
  status VARCHAR(50) NOT NULL,
  started_at TIMESTAMP,
  finished_at TIMESTAMP
);

CREATE TABLE execution_steps (
  id VARCHAR(36) PRIMARY KEY,
  execution_id VARCHAR(36) NOT NULL,
  step_definition_id BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL,
  result_json CLOB,
  started_at TIMESTAMP,
  finished_at TIMESTAMP,
  attempts INT DEFAULT 0,
  CONSTRAINT fk_execution FOREIGN KEY (execution_id) REFERENCES executions(id)
);
