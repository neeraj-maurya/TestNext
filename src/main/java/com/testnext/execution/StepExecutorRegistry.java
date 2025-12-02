package com.testnext.execution;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Runtime registry of available step executors. Supports dynamic registration
 * at runtime.
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
        // For now, we can just return the SystemStepExecutor if it's a system step
        // But the registry is currently manual.
        // Ideally, we should have a unified way.
        // For this task, let's assume the caller knows how to find the executor,
        // or we inject SystemStepExecutor into the Registry.
        return executors.get(name);
    }
}
