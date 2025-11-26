package com.testnext.repository;

import com.testnext.model.ExecutionStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExecutionStepRepository extends JpaRepository<ExecutionStepEntity, UUID> {
    List<ExecutionStepEntity> findByExecutionIdOrderById(UUID executionId);
    List<ExecutionStepEntity> findByExecutionIdOrderByStartedAt(UUID executionId);
    List<ExecutionStepEntity> findByExecutionIdAndStatus(UUID executionId, String status);
}
