-- Bootstrap seed data for H2 (dev profile)
-- MERGE INTO ... KEY(username) is H2's idempotent upsert syntax.
-- Runs on every startup; only inserts/updates if the username doesn't exist.

MERGE INTO system_users (id, username, email, display_name, role, hashed_password, active, created_at)
KEY(username)
VALUES (
    RANDOM_UUID(),
    'neera',
    'neera@example.com',
    'System Admin',
    'ROLE_SYSTEM_ADMIN',
    '$2a$10$9F0ujxrLfmfMaRBO29NKve0Y5/gyfI2tPJ0ZI6jB3NsUWSO.TBioC',
    true,
    NOW()
);
