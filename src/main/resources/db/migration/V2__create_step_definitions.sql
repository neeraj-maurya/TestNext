CREATE TABLE step_definitions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  definition CLOB,
  created_at TIMESTAMP
);
