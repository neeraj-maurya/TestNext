package com.testnext.permissions;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ProjectPermissionService {
    private final JdbcTemplate jdbc;

    public ProjectPermissionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Checks if the authenticated principal is a PROJECT_ADMIN for the given project in the current tenant.
     * This method relies on TenantContext (search_path) being set by TenantExtractionFilter.
     */
    public boolean isProjectAdmin(String projectId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        String username = authentication.getName();
        Integer cnt = jdbc.queryForObject(
                "select count(1) from users where username = ? and role = 'PROJECT_ADMIN' and project_id = ?::uuid",
                Integer.class, username, projectId);
        return cnt != null && cnt > 0;
    }
}
