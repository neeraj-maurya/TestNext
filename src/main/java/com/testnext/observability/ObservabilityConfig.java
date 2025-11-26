package com.testnext.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
// Actuator health types are provided by Spring Boot Actuator. For Spring Boot 4 some
// health APIs may differ; the simple tenant health indicator was removed to
// avoid compile-time coupling during the upgrade. Custom health checks can be
// reintroduced after aligning with the actuator API surface.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Observability configuration for Spring Boot 3.5+ with Micrometer metrics
 * and custom health indicators. These patterns will carry forward to Spring Boot 4.0.
 */
@Configuration
public class ObservabilityConfig {

    /**
     * (Note) Tenant schema health indicator intentionally omitted during SB4
     * upgrade because the actuator health API changed; re-add when ready.

    /**
     * Custom metrics for execution tracking.
     * Exposes counters and timers for execution lifecycle events.
     */
    @Bean
    public ExecutionMetrics executionMetrics(MeterRegistry meterRegistry) {
        return new ExecutionMetrics(meterRegistry);
    }

    /**
     * Metrics class for tracking execution events.
     * Used by ExecutionService and ExecutionEngine to record metrics.
     */
    public static class ExecutionMetrics {
        private final Counter executionsStarted;
        private final Counter executionsCompleted;
        private final Counter executionsFailed;
        private final Timer executionDuration;

        public ExecutionMetrics(MeterRegistry meterRegistry) {
            this.executionsStarted = Counter.builder("execution.started")
                    .description("Total number of executions started")
                    .register(meterRegistry);

            this.executionsCompleted = Counter.builder("execution.completed")
                    .description("Total number of executions completed successfully")
                    .register(meterRegistry);

            this.executionsFailed = Counter.builder("execution.failed")
                    .description("Total number of executions that failed")
                    .register(meterRegistry);

            this.executionDuration = Timer.builder("execution.duration")
                    .description("Duration of test executions")
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(meterRegistry);
        }

        public void recordExecutionStarted() {
            executionsStarted.increment();
        }

        public void recordExecutionCompleted() {
            executionsCompleted.increment();
        }

        public void recordExecutionFailed() {
            executionsFailed.increment();
        }

        public Timer.Sample recordExecutionStart() {
            return Timer.start();
        }

        public void recordExecutionStop(Timer.Sample sample) {
            sample.stop(executionDuration);
        }
    }
}
