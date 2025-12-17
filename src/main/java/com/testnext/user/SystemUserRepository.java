package com.testnext.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, UUID> {
    Optional<SystemUser> findByUsername(String username);

    Optional<SystemUser> findByApiKey(String apiKey);

    java.util.List<SystemUser> findByTenantId(Long tenantId);

    java.util.List<SystemUser> findByTenantIdAndRole(Long tenantId, String role);
}
