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
        java.util.Set<String> processedIds = new java.util.HashSet<>();

        for (Object bean : beans.values()) {
            for (Method method : bean.getClass().getMethods()) {
                if (method.isAnnotationPresent(TestStep.class)) {
                    TestStep annotation = method.getAnnotation(TestStep.class);
                    String id = annotation.id();

                    // Duplicate ID Check
                    if (processedIds.contains(id)) {
                        throw new IllegalStateException(
                                "Duplicate Test Step ID found: " + id + ". Application failed to start.");
                    }
                    processedIds.add(id);

                    String name = annotation.name();
                    String description = annotation.description();

                    // Reflection: Input Types
                    java.util.List<java.util.Map<String, Object>> inputList = new java.util.ArrayList<>();
                    java.util.List<String> paramTypeNames = new java.util.ArrayList<>();

                    java.lang.reflect.Parameter[] parameters = method.getParameters();
                    for (java.lang.reflect.Parameter param : parameters) {
                        // Skipping Context/Executor params if any (assuming all are inputs for now
                        // based on request)
                        String typeName = param.getType().getSimpleName();
                        java.util.Map<String, Object> map = new java.util.HashMap<>();
                        map.put("name", param.getName());
                        map.put("type", typeName);
                        map.put("required", true); // Default to true
                        inputList.add(map);
                        paramTypeNames.add(typeName);
                    }
                    String parameterTypes = String.join(", ", paramTypeNames);

                    // Reflection: Return Type
                    String returnType = method.getReturnType().getSimpleName();
                    if ("void".equalsIgnoreCase(returnType)) {
                        returnType = "Void";
                    }

                    // Register with executor (Legacy support if needed, or if executor uses ID now)
                    // For now, executor uses name, so we keep that.
                    executor.register(name, bean, method);

                    // Sync DB
                    StepDefinitionDto dto = new StepDefinitionDto();
                    dto.refId = id;
                    dto.name = name;
                    dto.description = description;
                    dto.inputs = inputList;
                    dto.returnType = returnType;
                    dto.parameterTypes = parameterTypes.isEmpty() ? "None" : parameterTypes;

                    stepService.sync(dto);
                }
            }
        }
    }
}
