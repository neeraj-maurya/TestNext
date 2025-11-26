package com.testnext.db;

import com.testnext.tenant.SchemaNameValidator;
import com.testnext.tenant.TenantContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// DataSource wrapper that sets search_path per connection. Uses SchemaNameValidator to sanitize and validate.
public class TenantRoutingDataSource implements DataSource {
    private final DataSource delegate;
    private final SchemaNameValidator schemaValidator; // optional, can be null for basic validation

    public TenantRoutingDataSource(DataSource delegate) {
        this(delegate, null);
    }

    public TenantRoutingDataSource(DataSource delegate, SchemaNameValidator schemaValidator) {
        this.delegate = delegate;
        this.schemaValidator = schemaValidator;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection c = delegate.getConnection();
        TenantContext.getTenant().ifPresent(schema -> applyTenantSchema(c, schema));
        return c;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection c = delegate.getConnection(username, password);
        TenantContext.getTenant().ifPresent(schema -> applyTenantSchema(c, schema));
        return c;
    }

    private void applyTenantSchema(Connection c, String schema) {
        try {
            String safeSchema = schema;
            if (schemaValidator != null) {
                // sanitize and validate against central registry; will throw IllegalArgumentException if invalid
                safeSchema = schemaValidator.sanitizeAndValidate(schema);
            } else {
                // Basic sanitation: lowercase and remove unsafe chars (best-effort). Prefer providing a SchemaNameValidator.
                safeSchema = schema.toLowerCase().replaceAll("[^a-z0-9_]+", "_");
            }
            // safeSchema should now match pattern [a-z][a-z0-9_]*
            c.createStatement().execute("SET search_path TO " + safeSchema + ",public");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ... delegate other DataSource methods

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }
    @Override
    public java.io.PrintWriter getLogWriter() throws SQLException { return delegate.getLogWriter(); }
    @Override
    public void setLogWriter(java.io.PrintWriter out) throws SQLException { delegate.setLogWriter(out); }
    @Override
    public void setLoginTimeout(int seconds) throws SQLException { delegate.setLoginTimeout(seconds); }
    @Override
    public int getLoginTimeout() throws SQLException { return delegate.getLoginTimeout(); }
    @Override
    public java.util.logging.Logger getParentLogger() { try { return delegate.getParentLogger(); } catch (Exception e) { throw new RuntimeException(e); } }
}
