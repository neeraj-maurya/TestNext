package com.testnext.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tests")
public class TestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "suite_id", nullable = false)
    public Long suiteId;

    @Column(nullable = false)
    public String name;
}
