package com.testnext.execution;

import com.testnext.annotation.TestStep;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SystemStepExecutor implements StepExecutor {
    private final Map<String, MethodHandler> handlers = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public void register(String stepName, Object bean, Method method) {
        handlers.put(stepName, new MethodHandler(bean, method));
    }

    @Override
    public StepResult execute(String stepName, Map<String, Object> parameters) throws Exception {
        MethodHandler handler = handlers.get(stepName);
        if (handler == null) {
            return new StepResult(false, null, "No handler found for step: " + stepName);
        }

        try {
            // Simple parameter mapping: assume method takes Map or specific args
            // For now, let's assume the method takes a Map<String, Object>
            Object result = handler.method.invoke(handler.bean, parameters);
            @SuppressWarnings("unchecked")
            Map<String, Object> mapResult = (Map<String, Object>) result;
            return new StepResult(true, mapResult, null);
        } catch (Exception e) {
            return new StepResult(false, null, e.getMessage());
        }
    }

    private static class MethodHandler {
        final Object bean;
        final Method method;

        MethodHandler(Object bean, Method method) {
            this.bean = bean;
            this.method = method;
        }
    }
}
