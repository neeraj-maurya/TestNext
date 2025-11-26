package com.testnext.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testnext.execution.ExecutionResult;
import com.testnext.execution.TestPlan;
import redis.clients.jedis.Jedis;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Enqueue-only Redis job queue. Serializes the TestPlan to JSON and pushes to a Redis list.
 * Returns a completed Future with the executionId immediately (clients should poll for real results).
 */
public class RedisJobQueue implements JobQueue {
    private final Jedis jedis;
    private final ObjectMapper mapper;
    private final String queueKey = "testnext:queue";

    public RedisJobQueue(Jedis jedis, ObjectMapper mapper) {
        this.jedis = jedis;
        this.mapper = mapper;
    }

    @Override
    public Future<ExecutionResult> submit(TestPlan plan) {
        try {
            String json = mapper.writeValueAsString(plan);
            jedis.lpush(queueKey, json);
        } catch (Exception e) {
            // best-effort: log and fallback to no-op
            System.err.println("Failed to enqueue plan to Redis: " + e.getMessage());
        }
        // return immediate placeholder; worker will perform the real execution asynchronously
        CompletableFuture<ExecutionResult> f = CompletableFuture.completedFuture(new ExecutionResult(plan.getExecutionId()));
        return f;
    }
}
