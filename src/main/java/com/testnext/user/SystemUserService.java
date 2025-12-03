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
        if (in.getId() == null) {
            in.setId(UUID.randomUUID());
        }
        if (in.getHashedPassword() != null && !in.getHashedPassword().isEmpty()) {
            // Store plain text password as requested
            in.setHashedPassword(in.getHashedPassword());
        } else {
            // default password for dev/testing if none provided
            in.setHashedPassword(in.getUsername() + "123");
        }
        return repo.save(in);
    }

    @Transactional
    public SystemUser update(UUID id, SystemUser in) {
        return repo.findById(id).map(u -> {
            if (in.getEmail() != null)
                u.setEmail(in.getEmail());
            if (in.getDisplayName() != null)
                u.setDisplayName(in.getDisplayName());

            if (in.getRole() != null && !in.getRole().equals(u.getRole())) {
                validateRole(in.getRole());
                // Hierarchy Rule: Cannot demote the last System Admin
                if ("ROLE_SYSTEM_ADMIN".equals(u.getRole())) {
                    long adminCount = repo.findAll().stream()
                            .filter(user -> "ROLE_SYSTEM_ADMIN".equals(user.getRole()))
                            .count();
                    if (adminCount <= 1) {
                        throw new IllegalStateException("Cannot demote the last System Admin");
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
