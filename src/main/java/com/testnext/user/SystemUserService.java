package com.testnext.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SystemUserService {
    private final SystemUserRepository repo;
    private final com.testnext.repository.TenantRepository tenantRepo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public SystemUserService(SystemUserRepository repo,
            com.testnext.repository.TenantRepository tenantRepo,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.tenantRepo = tenantRepo;
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
            in.setHashedPassword(in.getHashedPassword());
        } else {
            in.setHashedPassword(in.getUsername() + "123");
        }

        SystemUser saved = repo.save(in);

        // Sync Tenant Manager Logic (Create)
        syncTenantManagerOnCreate(saved);

        return saved;
    }

    @Transactional
    public SystemUser update(UUID id, SystemUser in) {
        return repo.findById(id).map(u -> {
            boolean wasManager = "ROLE_TEST_MANAGER".equals(u.getRole());
            Long oldTenantId = u.getTenantId();

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

            // Determine effective role to check for Admin status
            String roleToCheck = in.getRole() != null ? in.getRole() : u.getRole();

            // Tenant Logic: Strictly enforce NULL tenant for System Admins
            if ("ROLE_SYSTEM_ADMIN".equals(roleToCheck)) {
                u.setTenantId(null);
            } else if (in.getTenantId() != null) {
                // Only allow setting tenant if NOT an admin
                u.setTenantId(in.getTenantId());
            }

            // Update Active Status
            u.setActive(in.isActive());

            if (in.getRole() != null && !in.getRole().equals(u.getRole())) {
                validateRole(in.getRole());

                // Tenant Validation for Role Change
                if (!"ROLE_SYSTEM_ADMIN".equals(in.getRole())) {
                    if (u.getTenantId() == null && in.getTenantId() == null) {
                        throw new IllegalArgumentException("Tenant is required when changing to role: " + in.getRole());
                    }
                }

                if ("ROLE_SYSTEM_ADMIN".equals(u.getRole())) {
                    long adminCount = repo.findAll().stream()
                            .filter(user -> "ROLE_SYSTEM_ADMIN".equals(user.getRole()) && user.isActive())
                            .count();
                    if (adminCount <= 1) {
                        throw new IllegalStateException(
                                "Cannot change role: logic requires at least one active System Admin. Please create another Admin first.");
                    }
                }
                u.setRole(in.getRole());
            }

            if (in.getHashedPassword() != null && !in.getHashedPassword().isEmpty()) {
                u.setHashedPassword(passwordEncoder.encode(in.getHashedPassword()));
            }

            SystemUser updated = repo.save(u);
            syncTenantManagerOnUpdate(updated, wasManager, oldTenantId);
            return updated;
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void delete(UUID id) {
        SystemUser u = repo.findById(id).orElse(null);
        if (u != null) {
            if ("ROLE_SYSTEM_ADMIN".equals(u.getRole())) {
                long adminCount = repo.findAll().stream()
                        .filter(user -> "ROLE_SYSTEM_ADMIN".equals(user.getRole()))
                        .count();
                if (adminCount <= 1) {
                    throw new IllegalStateException("Cannot delete the last System Admin");
                }
            }
            // Sync Tenant Manager Logic (Delete)
            syncTenantManagerOnDelete(u);
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

    // --- Tenant Manager Sync Logic ---

    private void syncTenantManagerOnCreate(SystemUser user) {
        if ("ROLE_TEST_MANAGER".equals(user.getRole()) && user.getTenantId() != null) {
            tenantRepo.findById(user.getTenantId()).ifPresent(t -> {
                if (t.getTestManagerId() == null) {
                    t.setTestManagerId(user.getId());
                    tenantRepo.save(t);
                }
            });
        }
    }

    private void syncTenantManagerOnUpdate(SystemUser user, boolean wasManager, Long oldTenantId) {
        boolean isManager = "ROLE_TEST_MANAGER".equals(user.getRole());

        // Case 1: Tenant Change
        if (oldTenantId != null && !oldTenantId.equals(user.getTenantId())) {
            handleManagerRemoval(oldTenantId, user.getId());
            if (isManager)
                syncTenantManagerOnCreate(user); // Treat as new in new tenant
            return;
        }

        // Case 2: Demotion (Manager -> Impl/Viewer)
        if (wasManager && !isManager) {
            handleManagerRemoval(user.getTenantId(), user.getId());
        }

        // Case 3: Promotion (Other -> Manager)
        if (!wasManager && isManager) {
            syncTenantManagerOnCreate(user);
        }
    }

    private void syncTenantManagerOnDelete(SystemUser user) {
        if ("ROLE_TEST_MANAGER".equals(user.getRole())) {
            handleManagerRemoval(user.getTenantId(), user.getId());
        }
    }

    private void handleManagerRemoval(Long tenantId, UUID userId) {
        if (tenantId == null)
            return;

        tenantRepo.findById(tenantId).ifPresent(t -> {
            if (userId.equals(t.getTestManagerId())) {
                // Determine new manager
                UUID newManagerId = findReplacementManager(tenantId, userId);
                t.setTestManagerId(newManagerId);
                tenantRepo.save(t);
            }
        });
    }

    private UUID findReplacementManager(Long tenantId, UUID excludedUserId) {
        List<SystemUser> managers = repo.findByTenantIdAndRole(tenantId, "ROLE_TEST_MANAGER");
        return managers.stream()
                .filter(u -> !u.getId().equals(excludedUserId) && u.isActive())
                .findFirst()
                .map(SystemUser::getId)
                .orElse(null);
    }
}
