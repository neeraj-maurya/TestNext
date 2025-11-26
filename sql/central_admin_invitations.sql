-- Invitation table for onboarding admin users to tenants
CREATE TABLE IF NOT EXISTS public.tenant_invitations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL,
  email TEXT NOT NULL,
  token TEXT NOT NULL,
  expires_at TIMESTAMP WITH TIME ZONE,
  invited_by UUID,
  status TEXT DEFAULT 'pending', -- pending, accepted, expired
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  FOREIGN KEY (tenant_id) REFERENCES public.admin_tenants(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_tenant_invitations_email ON public.tenant_invitations(email);
