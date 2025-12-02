package com.testnext.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "step_definitions")
public class StepDefinitionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Lob
    public String definition;

    @Lob
    public String inputsJson;

    public Instant createdAt = Instant.now();
}
