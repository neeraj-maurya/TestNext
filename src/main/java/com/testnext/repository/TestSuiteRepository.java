package com.testnext.repository;

import com.testnext.model.TestSuiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestSuiteRepository extends JpaRepository<TestSuiteEntity, Long> {
    java.util.List<TestSuiteEntity> findByProjectId(Long projectId);
}
