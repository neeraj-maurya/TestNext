package com.testnext.repository;

import com.testnext.model.ExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<ExecutionEntity, UUID> {
}
