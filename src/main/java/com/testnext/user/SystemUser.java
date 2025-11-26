package com.testnext.user;

import java.time.OffsetDateTime;
import java.util.UUID;

// Simple DTO representing a system admin user stored in public.system_users
public class SystemUser {
    private UUID id;
    private String username;
    private String email;
    private String hashedPassword;
    private String displayName;
    private String role; // e.g., SYSTEM_ADMIN
    private OffsetDateTime createdAt;

    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
