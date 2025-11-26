Flyway per-tenant migration notes:

- Place tenant migrations under a folder and use Flyway API to run them with `.schemas(tenantSchema)`.
- Example migration file `sql/V1__tenant_init.sql` included as a starting point.
- When provisioning a tenant, call `FlywayTenantMigrator.migrate(schema)` after creating the schema.
