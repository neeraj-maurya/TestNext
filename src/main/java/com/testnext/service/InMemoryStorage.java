package com.testnext.service;

import com.testnext.api.dto.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.*;

public class InMemoryStorage {
    public final Map<Long, TenantDto> tenants = new LinkedHashMap<>();
    public final Map<Long, ProjectDto> projects = new LinkedHashMap<>();
    public final Map<Long, StepDefinitionDto> stepDefs = new LinkedHashMap<>();
    public final Map<Long, TestSuiteDto> suites = new LinkedHashMap<>();
    public final Map<Long, TestDto> tests = new LinkedHashMap<>();
    public final Map<Long, ExecutionDto> executions = new LinkedHashMap<>();

    public final AtomicLong seq = new AtomicLong(1);

    private static final InMemoryStorage INSTANCE = new InMemoryStorage();
    public static InMemoryStorage get() { return INSTANCE; }
    private InMemoryStorage() {}
}
