package com.testnext.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testnext.execution.ExecutionEngine;
import com.testnext.execution.TestPlan;
import redis.clients.jedis.Jedis;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Background worker that blocks on Redis list and executes plans as they arrive.
 * For local testing only; production should run a separate worker process/service.
 */
public class RedisWorker {
    private final Jedis jedis;
    private final ObjectMapper mapper;
    private final ExecutionEngine engine;
    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    public RedisWorker(Jedis jedis, ObjectMapper mapper, ExecutionEngine engine) {
        this.jedis = jedis; this.mapper = mapper; this.engine = engine;
    }

    public void start() {
        pool.submit(() -> {
            while (running) {
                try {
                    java.util.List<String> res = jedis.brpop(0, "testnext:queue");
                    if (res != null && res.size() >= 2) {
                        String json = res.get(1);
                        TestPlan plan = mapper.readValue(json, TestPlan.class);
                        engine.runTest(plan);
                    }
                } catch (Exception e) {
                    // sleep briefly then continue
                    try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }
        });
    }

    public void stop() { running = false; pool.shutdownNow(); }
}
