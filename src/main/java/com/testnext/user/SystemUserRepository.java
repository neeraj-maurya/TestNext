package com.testnext.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.UUID;

@Repository
public class SystemUserRepository {
    private final JdbcTemplate jdbc;

    public SystemUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SystemUser findByUsername(String username) {
        try {
            return jdbc.queryForObject("select id, username, email, hashed_password, display_name, role, created_at from public.system_users where username = ?",
                    (rs, rowNum) -> mapRow(rs), username);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private SystemUser mapRow(ResultSet rs) throws SQLException {
        SystemUser u = new SystemUser();
        u.setId((UUID) rs.getObject("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setHashedPassword(rs.getString("hashed_password"));
        u.setDisplayName(rs.getString("display_name"));
        u.setRole(rs.getString("role"));
        java.sql.Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toInstant().atOffset(ZoneOffset.UTC));
        return u;
    }
}
