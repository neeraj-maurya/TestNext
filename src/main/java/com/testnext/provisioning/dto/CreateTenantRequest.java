package com.testnext.provisioning.dto;

public class CreateTenantRequest {
    private String name;
    private String domain;
    private String adminEmail;
    private String preferredSchemaName;

    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getPreferredSchemaName() { return preferredSchemaName; }
    public void setPreferredSchemaName(String preferredSchemaName) { this.preferredSchemaName = preferredSchemaName; }
}
