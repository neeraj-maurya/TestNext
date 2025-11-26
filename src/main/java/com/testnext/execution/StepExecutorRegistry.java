package com.testnext.execution;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Runtime registry of available step executors. Supports dynamic registration at runtime.
 */
public class StepExecutorRegistry {
    private final Map<String, StepExecutor> executors = new ConcurrentHashMap<>();

    public void register(String name, StepExecutor executor) {
        executors.put(name, executor);
    }

    public void unregister(String name) {
        executors.remove(name);
    }

    public StepExecutor get(String name) {
        return executors.get(name);
    }
}
