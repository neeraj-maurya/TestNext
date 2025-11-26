package com.testnext.queue;

import com.testnext.execution.ExecutionResult;
import com.testnext.execution.TestPlan;

import java.util.concurrent.Future;

public interface JobQueue {
    Future<ExecutionResult> submit(TestPlan plan);
}
