-- Seed a system admin user in the central public schema for local dev/testing
INSERT INTO public.system_users (id, username, email, hashed_password, display_name, role, created_at)
VALUES (
  gen_random_uuid(),
  'sysadmin',
  'sysadmin@example.com',
  '{bcrypt}$2a$10$EXAMPLE_PLACEHOLDER_HASH', -- replace with bcrypt hash generated locally
  'System Admin',
  'SYSTEM_ADMIN',
  now()
);

-- Note: replace hashed_password value with a real bcrypt hash before production.
-- Example to create a bcrypt hash locally: use `htpasswd` or a small script with bcrypt.
