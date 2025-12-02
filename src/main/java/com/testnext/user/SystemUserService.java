package com.testnext.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SystemUserService {
    private final SystemUserRepository repo;

    public SystemUserService(SystemUserRepository repo) {
        this.repo = repo;
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
        if (in.getCreatedAt() == null) {
            in.setCreatedAt(OffsetDateTime.now());
        }
        // TODO: Hash password using PasswordEncoder
        if (in.getHashedPassword() == null || in.getHashedPassword().isEmpty()) {
            // fallback for dev
            in.setHashedPassword("{noop}" + (in.getUsername() + "123"));
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
            if (in.getRole() != null) {
                validateRole(in.getRole());
                u.setRole(in.getRole());
            }
            if (in.getHashedPassword() != null && !in.getHashedPassword().isEmpty()) {
                u.setHashedPassword(in.getHashedPassword());
            }
            return repo.save(u);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    public SystemUser findById(UUID id) {
        return repo.findById(id).orElse(null);
    }
}
