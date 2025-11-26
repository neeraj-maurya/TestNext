-- Flyway migration example for tenant schema: create core tables. Use placeholders if needed.
-- Place this under flyway migrations for per-tenant migration runs.

CREATE TABLE IF NOT EXISTS step_definitions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL UNIQUE,
  description TEXT,
  rest_endpoint TEXT,
  input_schema JSONB,
  output_schema JSONB,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS test_suites (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  project_id UUID,
  name TEXT NOT NULL
);
