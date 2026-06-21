package com.travelmind.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        addColumnIfMissing("travel_note_comment", "parent_id",
                "ALTER TABLE travel_note_comment ADD COLUMN parent_id BIGINT NULL AFTER user_id");
        addColumnIfMissing("travel_note_comment", "username",
                "ALTER TABLE travel_note_comment ADD COLUMN username VARCHAR(64) NULL AFTER parent_id");
        addColumnIfMissing("travel_note_comment", "avatar",
                "ALTER TABLE travel_note_comment ADD COLUMN avatar VARCHAR(1024) NULL AFTER username");
        addIndexIfMissing("travel_note_comment", "idx_comment_parent",
                "ALTER TABLE travel_note_comment ADD INDEX idx_comment_parent (parent_id)");

        addColumnIfMissing("ai_conversation", "summary",
                "ALTER TABLE ai_conversation ADD COLUMN summary LONGTEXT NULL");
        addColumnIfMissing("ai_conversation", "summary_message_count",
                "ALTER TABLE ai_conversation ADD COLUMN summary_message_count INT DEFAULT 0");
    }

    private void addColumnIfMissing(String table, String column, String sql) {
        var count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND column_name = ?
                """, Integer.class, table, column);
        if (count == null || count == 0) jdbcTemplate.execute(sql);
    }

    private void addIndexIfMissing(String table, String index, String sql) {
        var count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.statistics
                WHERE table_schema = DATABASE()
                  AND table_name = ?
                  AND index_name = ?
                """, Integer.class, table, index);
        if (count == null || count == 0) jdbcTemplate.execute(sql);
    }
}
