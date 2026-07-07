-- Bootstrap seed data for PostgreSQL
-- Runs on every startup; ON CONFLICT ensures it is idempotent (safe to re-run).

-- System admin user (neera / neera123 — bcrypt encoded)
INSERT INTO system_users (id, username, email, display_name, role, hashed_password, active, created_at)
VALUES (
    gen_random_uuid(),
    'neera',
    'neera@example.com',
    'System Admin',
    'ROLE_SYSTEM_ADMIN',
    '$2a$10$9F0ujxrLfmfMaRBO29NKve0Y5/gyfI2tPJ0ZI6jB3NsUWSO.TBioC',
    true,
    NOW()
)
ON CONFLICT (username) DO NOTHING;
