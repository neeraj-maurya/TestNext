package com.testnext.repository;

import com.testnext.model.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<ExecutionEntity, UUID> {
    @org.springframework.data.jpa.repository.Query("SELECT e FROM ExecutionEntity e JOIN TestEntity t ON e.testId = t.id JOIN TestSuiteEntity s ON t.suiteId = s.id WHERE s.projectId = :projectId ORDER BY e.startedAt DESC")
    java.util.List<ExecutionEntity> findByProjectId(Long projectId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM ExecutionEntity e JOIN TestEntity t ON e.testId = t.id JOIN TestSuiteEntity s ON t.suiteId = s.id JOIN ProjectEntity p ON s.projectId = p.id WHERE p.tenantId = :tenantId ORDER BY e.startedAt DESC")
    java.util.List<ExecutionEntity> findByTenantId(Long tenantId);
}
