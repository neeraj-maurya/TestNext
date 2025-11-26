-- Tenant template DDL
-- Run this in a new schema for each tenant, e.g. SET search_path TO ${schema}, public;

-- USERS
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  username TEXT NOT NULL,
  email TEXT NOT NULL,
  hashed_password TEXT,
  role TEXT NOT NULL,
  display_name TEXT,
  project_id UUID, -- optional FK to projects
  is_active BOOLEAN DEFAULT true,
  created_by UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users(email);

-- TENANT PROJECTS
CREATE TABLE IF NOT EXISTS projects (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  description TEXT,
  status TEXT DEFAULT 'active',
  created_by UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_projects_name ON projects(name);

-- TEST SUITES
CREATE TABLE IF NOT EXISTS test_suites (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  project_id UUID NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  status TEXT DEFAULT 'draft',
  created_by UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_test_suites_project ON test_suites(project_id);

-- TESTS
CREATE TABLE IF NOT EXISTS tests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  suite_id UUID NOT NULL,
  name TEXT NOT NULL,
  description TEXT,
  status TEXT DEFAULT 'draft',
  approval_status TEXT DEFAULT 'pending',
  created_by UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  FOREIGN KEY (suite_id) REFERENCES test_suites(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_tests_suite ON tests(suite_id);

-- STEP DEFINITIONS (shared within tenant)
CREATE TABLE IF NOT EXISTS step_definitions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL UNIQUE,
  description TEXT,
  rest_endpoint TEXT, -- optional endpoint this step calls
  input_schema JSONB, -- JSON schema or simple schema for dynamic forms
  output_schema JSONB,
  created_by UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- TEST STEPS (instances of step_definitions within a test)
CREATE TABLE IF NOT EXISTS test_steps (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  test_id UUID NOT NULL,
  step_definition_id UUID NOT NULL,
  step_order INT NOT NULL,
  description TEXT,
  parameters_json JSONB,
  is_dependent BOOLEAN DEFAULT false,
  depends_on_step_id UUID, -- optional
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE,
  FOREIGN KEY (step_definition_id) REFERENCES step_definitions(id)
);
CREATE INDEX IF NOT EXISTS idx_test_steps_test ON test_steps(test_id);

-- EXECUTIONS (orchestration level)
CREATE TABLE IF NOT EXISTS executions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  test_id UUID NOT NULL,
  triggered_by UUID,
  status TEXT NOT NULL DEFAULT 'queued', -- queued, running, success, failed
  started_at TIMESTAMP WITH TIME ZONE,
  finished_at TIMESTAMP WITH TIME ZONE,
  orchestration_metadata JSONB, -- concurrency, retry policy snapshot
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  FOREIGN KEY (test_id) REFERENCES tests(id)
);
CREATE INDEX IF NOT EXISTS idx_executions_test ON executions(test_id);

-- EXECUTION STEPS (per-step results)
CREATE TABLE IF NOT EXISTS execution_steps (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  execution_id UUID NOT NULL,
  test_step_id UUID NOT NULL,
  status TEXT NOT NULL DEFAULT 'queued', -- queued, running, success, failed, skipped
  result_json JSONB,
  started_at TIMESTAMP WITH TIME ZONE,
  finished_at TIMESTAMP WITH TIME ZONE,
  attempts INT DEFAULT 0,
  error_text TEXT,
  FOREIGN KEY (execution_id) REFERENCES executions(id) ON DELETE CASCADE,
  FOREIGN KEY (test_step_id) REFERENCES test_steps(id)
);
CREATE INDEX IF NOT EXISTS idx_execution_steps_exec ON execution_steps(execution_id);

-- AUDIT LOGS (append-only within tenant)
CREATE TABLE IF NOT EXISTS audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  entity_type TEXT NOT NULL,
  entity_id UUID,
  action TEXT NOT NULL, -- create, update, delete, execute
  user_id UUID,
  changed_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  changes_json JSONB
);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);

-- DELETED COMPONENTS HISTORY (logical deletion retention)
CREATE TABLE IF NOT EXISTS deleted_components_history (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  entity_type TEXT NOT NULL,
  entity_id UUID,
  deleted_by UUID,
  deleted_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  details_json JSONB
);
CREATE INDEX IF NOT EXISTS idx_deleted_components_entity ON deleted_components_history(entity_type, entity_id);

-- Optional performance: create GIN indices for JSONB where needed
CREATE INDEX IF NOT EXISTS idx_test_steps_parameters_gin ON test_steps USING GIN (parameters_json);
CREATE INDEX IF NOT EXISTS idx_execution_steps_result_gin ON execution_steps USING GIN (result_json);
