-- Sample step definitions to seed into each tenant schema
INSERT INTO step_definitions (id, name, description, rest_endpoint, input_schema, output_schema, created_by)
VALUES
  (gen_random_uuid(), 'HTTP Request', 'Execute HTTP request against an endpoint', '/exec/http-request',
    '{"fields":[{"name":"method","type":"string","label":"Method","enum":["GET","POST","PUT","DELETE"]},{"name":"url","type":"string","label":"URL"},{"name":"headers","type":"json","label":"Headers (JSON)"},{"name":"body","type":"json","label":"Body (JSON)"}]}'::jsonb,
    '{"fields":[{"name":"status","type":"integer"},{"name":"body","type":"json"}]}'::jsonb, NULL
  ),
  (gen_random_uuid(), 'Validate JWT Expiry', 'Validate JWT expiration', '/exec/validate-jwt',
    '{"fields":[{"name":"token","type":"string","label":"JWT Token"}]}'::jsonb,
    '{"fields":[{"name":"isExpired","type":"boolean"},{"name":"expiry","type":"string"}]}'::jsonb, NULL
  );

-- Note: run this after creating tenant schema with tenant_template.sql and setting search_path to tenant schema.
