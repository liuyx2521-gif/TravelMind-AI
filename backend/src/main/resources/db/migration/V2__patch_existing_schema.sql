DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;

DELIMITER //

CREATE PROCEDURE add_column_if_missing(
    IN table_name_value VARCHAR(64),
    IN column_name_value VARCHAR(64),
    IN column_definition_value TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = table_name_value
          AND column_name = column_name_value
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', table_name_value, '` ADD COLUMN ', column_definition_value);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

CREATE PROCEDURE add_index_if_missing(
    IN table_name_value VARCHAR(64),
    IN index_name_value VARCHAR(64),
    IN index_definition_value TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = table_name_value
          AND index_name = index_name_value
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', table_name_value, '` ADD ', index_definition_value);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//

DELIMITER ;

CALL add_column_if_missing('travel_note', 'images', '`images` LONGTEXT');

CALL add_column_if_missing('travel_note_comment', 'parent_id', '`parent_id` BIGINT');
CALL add_column_if_missing('travel_note_comment', 'username', '`username` VARCHAR(64)');
CALL add_column_if_missing('travel_note_comment', 'avatar', '`avatar` VARCHAR(1024)');
CALL add_index_if_missing('travel_note_comment', 'idx_comment_parent', 'INDEX `idx_comment_parent` (`parent_id`)');

CALL add_column_if_missing('favorite', 'cover', '`cover` VARCHAR(1024)');
CALL add_column_if_missing('favorite', 'path', '`path` VARCHAR(255)');

CALL add_column_if_missing('browse_history', 'cover', '`cover` VARCHAR(1024)');
CALL add_column_if_missing('browse_history', 'path', '`path` VARCHAR(255)');

CALL add_column_if_missing('travel_plan', 'content', '`content` LONGTEXT');

CALL add_column_if_missing('ai_conversation', 'summary', '`summary` LONGTEXT');
CALL add_column_if_missing('ai_conversation', 'summary_message_count', '`summary_message_count` INT DEFAULT 0');

DROP PROCEDURE IF EXISTS add_column_if_missing;
DROP PROCEDURE IF EXISTS add_index_if_missing;
