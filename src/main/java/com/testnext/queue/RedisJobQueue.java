package com.testnext.queue;

import com.testnext.execution.ExecutionResult;
import com.testnext.execution.TestPlan;
import java.util.concurrent.Future;

/**
 * Dummy RedisJobQueue to bypass Jedis dependency issues during testing.
 */
public class RedisJobQueue implements JobQueue {

    public RedisJobQueue() {
    }

    @Override
    public Future<ExecutionResult> submit(TestPlan plan) {
        throw new UnsupportedOperationException("Redis not available - Jedis dependency missing");
    }
}
