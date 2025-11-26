CREATE TABLE test_steps (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  test_id BIGINT NOT NULL,
  step_definition_id BIGINT NOT NULL,
  parameters CLOB
);

CREATE TABLE executions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  test_id BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL,
  started_at TIMESTAMP,
  finished_at TIMESTAMP
);

CREATE TABLE execution_steps (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  execution_id BIGINT NOT NULL,
  step_definition_id BIGINT NOT NULL,
  status VARCHAR(50) NOT NULL,
  result CLOB
);
