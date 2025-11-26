package com.testnext.repository;

import com.testnext.model.TestStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestStepRepository extends JpaRepository<TestStepEntity, Long> {
    List<TestStepEntity> findByTestIdOrderById(Long testId);
}
