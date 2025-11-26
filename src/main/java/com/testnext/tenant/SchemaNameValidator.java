package com.testnext.tenant;

import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * Validates and sanitizes tenant schema names. Use this to avoid SQL injection and accidental misuse.
 * Two-step approach:
 *  - sanitize: make a best-effort canonical name from free-form input
 *  - exists/validate: optionally check central admin_tenants to ensure schema is registered
 */
@Component
public class SchemaNameValidator {
    private static final Pattern SAFE = Pattern.compile("^[a-z][a-z0-9_]{1,60}$");

    private final JdbcTemplate centralJdbc; // connected to public schema (may be null in tests)

    public SchemaNameValidator(DataSource centralDataSource) {
        if (centralDataSource == null) {
            this.centralJdbc = null;
        } else {
            this.centralJdbc = new JdbcTemplate(centralDataSource);
        }
    }

    public String sanitize(String candidate) {
        if (candidate == null) return null;
        String s = candidate.trim().toLowerCase().replaceAll("[^a-z0-9_]+", "_");
        if (!s.matches("^[a-z].*")) s = "t_" + s;
        if (s.length() > 60) s = s.substring(0, 60);
        return s;
    }

    public boolean patternValid(String schema) {
        if (schema == null) return false;
        return SAFE.matcher(schema).matches();
    }

    public boolean existsInRegistry(String schema) {
        if (centralJdbc == null) return false;
        try {
            Integer cnt = centralJdbc.queryForObject("select count(1) from public.admin_tenants where schema_name = ?", Integer.class, schema);
            return cnt != null && cnt > 0;
        } catch (Exception e) {
            // in case central DB isn't available, fail-safe to false
            return false;
        }
    }

    /**
     * Sanitize and validate: returns sanitized schema if OK; otherwise throws IllegalArgumentException.
     */
    public String sanitizeAndValidate(String candidate) {
        String s = sanitize(candidate);
        if (!patternValid(s)) throw new IllegalArgumentException("Invalid schema name after sanitization: " + s);
        // Optionally ensure the schema exists in admin registry
        if (!existsInRegistry(s)) throw new IllegalArgumentException("Schema does not exist in admin registry: " + s);
        return s;
    }
}
