package com.testnext.repository;

import com.testnext.model.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
    java.util.List<TestEntity> findBySuiteId(Long suiteId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM TestEntity t JOIN TestSuiteEntity s ON t.suiteId = s.id WHERE s.projectId = :projectId")
    java.util.List<TestEntity> findByProjectId(Long projectId);
}
