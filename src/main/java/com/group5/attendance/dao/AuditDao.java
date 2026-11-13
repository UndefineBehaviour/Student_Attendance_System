package com.group5.attendance.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** Writes the audit log: who did what, and when (table audit_log). */
@Repository
public class AuditDao {

    private final JdbcTemplate jdbc;

    public AuditDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void log(int actorId, String action, String entity, Integer entityId,
                    String oldValue, String newValue, String reason) {
        jdbc.update(
            "INSERT INTO audit_log (actor_id, action, entity, entity_id, old_value, new_value, reason) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?)",
            actorId, action, entity, entityId, cut(oldValue), cut(newValue), cut(reason));
    }

    /** The columns hold at most 255 characters. */
    private static String cut(String text) {
        return text == null || text.length() <= 255 ? text : text.substring(0, 252) + "...";
    }
}