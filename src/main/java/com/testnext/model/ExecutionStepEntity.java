package com.testnext.model;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "execution_steps")
public class ExecutionStepEntity {
    @Id
    @Column(length = 36)
    public UUID id;

    @Column(name = "execution_id", length = 36, nullable = false)
    public UUID executionId;

    @Column(name = "step_definition_id", nullable = false)
    public Long stepDefinitionId;

    @Column(nullable = false)
    public String status;

    @Lob
    public String resultJson;

    @Column(name = "started_at")
    public Instant startedAt;

    @Column(name = "finished_at")
    public Instant finishedAt;

    @Column(name = "attempts")
    public Integer attempts;
}
