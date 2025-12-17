package com.testnext.repository;

import com.testnext.model.StepDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepDefinitionRepository extends JpaRepository<StepDefinitionEntity, Long> {
    java.util.Optional<StepDefinitionEntity> findByName(String name);

    java.util.Optional<StepDefinitionEntity> findByRefId(String refId);
}
