package com.testnext.execution;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
// Only enable the execution startup wiring when the property 'testnext.execution.enabled' is set to true.
// Tests and other lightweight contexts should leave this unset to avoid scanning heavy execution wiring.
@ConditionalOnProperty(name = "testnext.execution.enabled", havingValue = "true", matchIfMissing = false)
public class StartupRegistrar {

    @Bean
    public StepExecutorRegistry stepExecutorRegistry() {
        StepExecutorRegistry r = new StepExecutorRegistry();
        r.register("http-request", new DefaultHttpStepExecutor());
        return r;
    }

    @Bean
    public ExecutionEngine executionEngine(StepExecutorRegistry registry, com.testnext.execution.ExecutionRepositoryI repo) {
        return new ExecutionEngine(10, registry, repo, objectMapper()); // default pool size 10
    }

    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }

    @Bean
    public com.testnext.queue.JobQueue jobQueue(ExecutionEngine engine) {
        // default: in-memory queue for local testing
        return new com.testnext.queue.InMemoryJobQueue(engine);
    }

    // Optional Redis wiring. To use Redis queue set property: testnext.queue.type=redis
    @Bean
    public redis.clients.jedis.Jedis jedis() {
        // default local redis on 6379
        return new redis.clients.jedis.Jedis("localhost", 6379);
    }

    @Bean
    public com.testnext.queue.RedisWorker redisWorker(redis.clients.jedis.Jedis jedis, com.fasterxml.jackson.databind.ObjectMapper mapper, ExecutionEngine engine) {
        com.testnext.queue.RedisWorker w = new com.testnext.queue.RedisWorker(jedis, mapper, engine);
        // do not start automatically unless explicitly invoked by the app bootstrap
        return w;
    }
}
