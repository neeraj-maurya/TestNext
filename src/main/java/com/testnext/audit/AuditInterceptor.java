package com.testnext.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Simple service to append audit logs. In a real app use JPA listeners or AOP around services.
 */
@Component
public class AuditInterceptor {
    private final JdbcTemplate jdbc;

    public AuditInterceptor(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void log(String entityType, UUID entityId, String action, UUID userId, String changesJson) {
        jdbc.update("INSERT INTO audit_logs (id, entity_type, entity_id, action, user_id, changed_at, changes_json) VALUES (gen_random_uuid(), ?, ?, ?, ?, ?, ?::jsonb)",
                entityType, entityId, action, userId, OffsetDateTime.now(), changesJson);
    }
}
