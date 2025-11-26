package com.testnext.model;

import jakarta.persistence.*;

@Entity
@Table(name = "test_suites")
public class TestSuiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "project_id", nullable = false)
    public Long projectId;

    @Column(nullable = false)
    public String name;

    @Column
    public String description;
}
