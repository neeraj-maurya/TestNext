package com.testnext.execution;

import com.testnext.annotation.TestStep;
import com.testnext.api.dto.StepDefinitionDto;
import com.testnext.service.StepDefinitionService;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Map;

@Component
public class SystemStepScanner {
    private final ApplicationContext context;
    private final SystemStepExecutor executor;
    private final StepDefinitionService stepService;

    public SystemStepScanner(ApplicationContext context, SystemStepExecutor executor,
            StepDefinitionService stepService) {
        this.context = context;
        this.executor = executor;
        this.stepService = stepService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationEvent() {
        Map<String, Object> beans = context.getBeansWithAnnotation(Component.class);
        for (Object bean : beans.values()) {
            for (Method method : bean.getClass().getMethods()) {
                if (method.isAnnotationPresent(TestStep.class)) {
                    TestStep annotation = method.getAnnotation(TestStep.class);
                    String name = annotation.name();
                    String description = annotation.description();

                    // Register with executor
                    executor.register(name, bean, method);

                    // Ensure it exists in DB
                    createIfMissing(name, description, annotation.inputs());
                }
            }
        }
    }

    private void createIfMissing(String name, String description, String[] inputs) {
        if (stepService.list().stream().noneMatch(s -> s.name.equals(name))) {
            StepDefinitionDto dto = new StepDefinitionDto();
            dto.name = name;
            dto.description = description;

            java.util.List<java.util.Map<String, Object>> inputList = new java.util.ArrayList<>();
            if (inputs != null) {
                for (String input : inputs) {
                    String[] parts = input.split(":");
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("name", parts[0]);
                    map.put("type", parts.length > 1 ? parts[1] : "string");
                    map.put("required", parts.length > 2 ? Boolean.parseBoolean(parts[2]) : false);
                    inputList.add(map);
                }
            }
            dto.inputs = inputList;

            stepService.create(dto);
        }
    }
}
