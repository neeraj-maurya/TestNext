package com.testnext.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system-users")
public class ApiSystemUsersController {
    private final JdbcTemplate jdbc;
    private static final Logger log = LoggerFactory.getLogger(ApiSystemUsersController.class);

    public ApiSystemUsersController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "select id, username, email, display_name, role, created_at from system_users order by id");
            // Normalize keys to lowercase
            return rows.stream().map(this::toLowerCaseKeys).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to list system users", e);
            return List.of();
        }
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> in) {
        try {
            String username = (String) in.get("username");
            String email = (String) in.get("email");
            String role = (String) in.getOrDefault("role", "SYSTEM_ADMIN");
            String displayName = (String) in.getOrDefault("displayName", "");

            // Validation
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username is required");
            }
            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                throw new IllegalArgumentException("Valid email is required");
            }

            // Ensure table exists in dev
            jdbc.execute("CREATE TABLE IF NOT EXISTS system_users (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(255) NOT NULL UNIQUE, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "hashed_password VARCHAR(255), " +
                    "display_name VARCHAR(255), " +
                    "role VARCHAR(50) NOT NULL DEFAULT 'SYSTEM_ADMIN', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            jdbc.update(
                    "insert into system_users (username, email, hashed_password, display_name, role, created_at) values (?, ?, ?, ?, ?, ?)",
                    username, email, in.getOrDefault("hashedPassword", ""), displayName, role,
                    Timestamp.from(java.time.Instant.now()));

            var row = jdbc.queryForMap(
                    "select id, username, email, display_name, role, created_at from system_users where username = ?",
                    username);
            return toLowerCaseKeys(row);
        } catch (Exception e) {
            log.error("Failed to create system user: {}", in, e);
            return Map.of("error", e.getMessage());
        }
    }

    private Map<String, Object> toLowerCaseKeys(Map<String, Object> row) {
        java.util.Map<String, Object> newRow = new java.util.HashMap<>();
        row.forEach((k, v) -> newRow.put(k.toLowerCase(), v));
        // Map display_name to displayName for frontend consistency if needed,
        // but frontend handles display_name || displayName.
        // Let's keep it simple: lowercase keys.
        return newRow;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        try {
            jdbc.update("delete from system_users where id = ?", id);
        } catch (Exception e) {
            // silently fail if table doesn't exist
        }
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Map<String, Object> in) {
        try {
            String username = (String) in.get("username");
            String email = (String) in.get("email");
            String role = (String) in.get("role");
            String displayName = (String) in.get("displayName");

            // Update fields if provided
            if (username != null)
                jdbc.update("update system_users set username = ? where id = ?", username, id);
            if (email != null)
                jdbc.update("update system_users set email = ? where id = ?", email, id);
            if (role != null)
                jdbc.update("update system_users set role = ? where id = ?", role, id);
            if (displayName != null)
                jdbc.update("update system_users set display_name = ? where id = ?", displayName, id);

            return toLowerCaseKeys(jdbc.queryForMap(
                    "select id, username, email, display_name, role, created_at from system_users where id = ?", id));
        } catch (Exception e) {
            log.error("Failed to update system user: {}", in, e);
            throw new RuntimeException("Failed to update user", e);
        }
    }
}
