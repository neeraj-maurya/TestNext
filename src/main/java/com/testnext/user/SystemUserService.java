package com.testnext.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SystemUserService {
    private final SystemUserRepository repo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public SystemUserService(SystemUserRepository repo,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<SystemUser> list() {
        return repo.findAll();
    }

    private static final java.util.Set<String> ALLOWED_ROLES = java.util.Set.of(
            "ROLE_SYSTEM_ADMIN", "ROLE_TEST_MANAGER", "ROLE_TEST_ENGINEER", "ROLE_VIEWER");

    private void validateRole(String role) {
        if (role != null && !ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    @Transactional
    public SystemUser create(SystemUser in) {
        validateRole(in.getRole());

        // Tenant Validation
        if (!"ROLE_SYSTEM_ADMIN".equals(in.getRole())) {
            if (in.getTenantId() == null) {
                throw new IllegalArgumentException("Tenant is required for role: " + in.getRole());
            }
        }

        if (in.getId() == null) {
            in.setId(UUID.randomUUID());
        }

        if (in.getCreatedAt() == null) {
            in.setCreatedAt(OffsetDateTime.now());
        }

        if (in.getHashedPassword() != null && !in.getHashedPassword().isEmpty()) {
            // Store plain text password as requested
            in.setHashedPassword(in.getHashedPassword());
        } else {
            // default password for dev/testing if none provided
            in.setHashedPassword(in.getUsername() + "123");
        }

        // Ensure active is set (default is true in entity but good to be explicit if
        // passed in JSON)
        // in.isActive() is relied upon

        return repo.save(in);
    }

    @Transactional
    public SystemUser update(UUID id, SystemUser in) {
        return repo.findById(id).map(u -> {
            // Handle Username Update
            if (in.getUsername() != null && !in.getUsername().isBlank() && !in.getUsername().equals(u.getUsername())) {
                if (repo.findByUsername(in.getUsername()).isPresent()) {
                    throw new IllegalArgumentException("Username '" + in.getUsername() + "' is already taken.");
                }
                u.setUsername(in.getUsername());
            }

            if (in.getEmail() != null)
                u.setEmail(in.getEmail());
            if (in.getDisplayName() != null)
                u.setDisplayName(in.getDisplayName());

            // Allow updating tenantId
            if (in.getTenantId() != null) {
                u.setTenantId(in.getTenantId());
            } else if ("ROLE_SYSTEM_ADMIN".equals(in.getRole())) {
                // Admins can clear tenant? Maybe. For now allow explicit set.
            }

            // Update Active Status
            u.setActive(in.isActive());

            if (in.getRole() != null && !in.getRole().equals(u.getRole())) {
                validateRole(in.getRole());

                // Tenant Validation for Role Change
                if (!"ROLE_SYSTEM_ADMIN".equals(in.getRole())) {
                    // New role requires tenant. Check if we have one.
                    if (u.getTenantId() == null && in.getTenantId() == null) {
                        throw new IllegalArgumentException("Tenant is required when changing to role: " + in.getRole());
                    }
                }

                // Hierarchy Rule: Cannot demote the last System Admin
                if ("ROLE_SYSTEM_ADMIN".equals(u.getRole())) {
                    long adminCount = repo.findAll().stream()
                            .filter(user -> "ROLE_SYSTEM_ADMIN".equals(user.getRole()) && user.isActive())
                            .count();
                    if (adminCount <= 1) {
                        throw new IllegalStateException("Cannot demote the last Active System Admin");
                    }
                }
                u.setRole(in.getRole());
            }

            if (in.getHashedPassword() != null && !in.getHashedPassword().isEmpty()) {
                u.setHashedPassword(passwordEncoder.encode(in.getHashedPassword()));
            }
            return repo.save(u);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void delete(UUID id) {
        SystemUser u = repo.findById(id).orElse(null);
        if (u != null && "ROLE_SYSTEM_ADMIN".equals(u.getRole())) {
            long adminCount = repo.findAll().stream()
                    .filter(user -> "ROLE_SYSTEM_ADMIN".equals(user.getRole()))
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last System Admin");
            }
        }
        repo.deleteById(id);
    }

    public SystemUser findById(UUID id) {
        return repo.findById(id).orElse(null);
    }

    public SystemUser findByApiKey(String apiKey) {
        return repo.findByApiKey(apiKey).orElse(null);
    }

    public SystemUser findByUsername(String username) {
        return repo.findByUsername(username).orElse(null);
    }

    @Transactional
    public String generateApiKey(UUID userId) {
        SystemUser user = repo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        String newKey = "sk_" + UUID.randomUUID().toString().replace("-", "") + "_" + System.currentTimeMillis();
        user.setApiKey(newKey);
        repo.save(user);
        return newKey;
    }
}
