package com.testnext.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "executions")
public class ExecutionEntity {
    @Id
    @Column(length = 36)
    public UUID id;

    @Column(name = "test_id", nullable = false)
    public Long testId;

    @Column(nullable = false)
    public String status;

    @Column(name = "started_at")
    public Instant startedAt;

    @Column(name = "finished_at")
    public Instant finishedAt;
}
