package com.testnext.execution;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StepExecutorRegistryTest {
    @Test
    public void registerAndGet() {
        StepExecutorRegistry r = new StepExecutorRegistry();
        StepExecutor e = (stepDefId, params) -> new StepResult(true, null, null);
        r.register("noop", e);
        assertEquals(e, r.get("noop"));
    }
}
