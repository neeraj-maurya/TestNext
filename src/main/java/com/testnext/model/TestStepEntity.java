package com.testnext.model;

import jakarta.persistence.*;

@Entity
@Table(name = "test_steps")
public class TestStepEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "test_id", nullable = false)
    public Long testId;

    @Column(name = "step_definition_id", nullable = false)
    public Long stepDefinitionId;

    @Lob
    public String parametersJson; // optional JSON of parameters
}
