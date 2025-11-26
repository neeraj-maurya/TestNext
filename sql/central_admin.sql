-- Central management schema: stores tenant registry and global admin records
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS public.admin_tenants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  domain TEXT,
  schema_name TEXT NOT NULL UNIQUE,
  plan TEXT,
  status TEXT NOT NULL DEFAULT 'active',
  metadata JSONB,
  created_by UUID,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_admin_tenants_schema_name ON public.admin_tenants(schema_name);

-- Optional: global system administrators table (for cross-tenant system management)
CREATE TABLE IF NOT EXISTS public.system_users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  username TEXT NOT NULL UNIQUE,
  email TEXT NOT NULL UNIQUE,
  hashed_password TEXT,
  display_name TEXT,
  role TEXT NOT NULL DEFAULT 'system_admin',
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
