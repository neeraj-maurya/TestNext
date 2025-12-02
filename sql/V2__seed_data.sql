-- Seed data for development and testing

-- Insert Step Definitions
INSERT INTO step_definitions (name, description, rest_endpoint, input_schema, output_schema, created_at)
VALUES 
  ('HTTP GET Request', 'Execute HTTP GET request', '/api/steps/http-get', '{"url": "string"}', '{"status": "number", "body": "object"}', now()),
  ('HTTP POST Request', 'Execute HTTP POST request', '/api/steps/http-post', '{"url": "string", "body": "object"}', '{"status": "number", "body": "object"}', now()),
  ('Validate Response', 'Validate response contains expected content', '/api/steps/validate', '{"response": "object", "expected": "object"}', '{"valid": "boolean"}', now()),
  ('Login User', 'Authenticate user in system', '/api/steps/login', '{"username": "string", "password": "string"}', '{"token": "string", "user_id": "string"}', now()),
  ('Database Query', 'Execute database query', '/api/steps/db-query', '{"query": "string"}', '{"result": "object", "rows": "array"}', now()),
  ('Wait for Element', 'Wait for UI element to appear', '/api/steps/wait', '{"selector": "string", "timeout": "number"}', '{"found": "boolean"}', now())
ON CONFLICT (name) DO NOTHING;

-- Insert Test Suites
INSERT INTO test_suites (name)
VALUES 
  ('Smoke Tests'),
  ('Authentication Tests'),
  ('API Integration Tests'),
  ('Performance Tests'),
  ('Security Tests')
ON CONFLICT DO NOTHING;

-- Insert Tenants (for central admin context)
INSERT INTO tenants (name, schema_name, created_at, updated_at, is_active)
VALUES 
  ('Acme Corp', 'acme_corp', now(), now(), true),
  ('Tech Innovations Inc', 'tech_innov', now(), now(), true),
  ('Global Services Ltd', 'global_services', now(), now(), true),
  ('StartUp Demo', 'startup_demo', now(), now(), true)
ON CONFLICT DO NOTHING;
