Execution engine notes:

- `ExecutionEngine` runs TestPlans with a fixed-size thread pool.
- Steps may be dependent or independent. Independent steps can be parallelized; current implementation runs sequentially per plan for simplicity.
- Retry logic: `TestStep` has `maxAttempts` and `retryDelayMs`.
- Partial rerun: store execution state in `executions` and `execution_steps` tables. To rerun from failure: recreate a TestPlan from failed steps and run with restored context.

Next improvements:
- Parallelize independent steps using completion service or DAG execution.
- Persist execution steps state incrementally to the DB.
- Provide per-tenant concurrency throttling and global job queue (Redis-backed work queue or Kafka).