-- Run this inside a tenant schema (SET search_path TO your_schema, public)
-- Creates a sample project and a user with PROJECT_ADMIN role for that project
INSERT INTO projects (id, name, description, created_by, created_at)
VALUES (gen_random_uuid(), 'Sample Project', 'Seeded sample project', NULL, now());

-- Insert a tester with PROJECT_ADMIN role tied to the above project
INSERT INTO users (id, username, email, hashed_password, role, project_id, created_at)
VALUES (gen_random_uuid(), 'alice', 'alice@tenant.example.com', '{bcrypt}$2a$10$EXAMPLE_PLACEHOLDER_HASH', 'PROJECT_ADMIN', (select id from projects where name = 'Sample Project' limit 1), now());

-- Replace the bcrypt hash placeholders with actual bcrypt hashes for local testing.
