package com.testnext.execution;

/**
 * Compatibility alias for legacy code/tests that referenced
 * com.testnext.execution.ExecutionRepository. It simply extends
 * the newer ExecutionRepositoryI interface so existing tests
 * and code can continue to mock or inject this type.
 */
public interface ExecutionRepository extends ExecutionRepositoryI {

}

