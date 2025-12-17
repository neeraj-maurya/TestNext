package com.testnext.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "test_steps_library")
public class StepDefinitionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public String refId;

    @Column(nullable = false)
    public String name;

    @Lob
    public String description;

    @Lob
    public String definition; // Kept for legacy compatibility if needed, but scanner uses refId to find bean

    @Lob
    public String inputsJson; // Parameters Schema

    public String returnType;

    public String parameterTypes; // Human readable summary of inputs

    public Instant createdAt = Instant.now();
}
