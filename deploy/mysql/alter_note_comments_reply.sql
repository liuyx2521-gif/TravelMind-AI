USE travelmind_ai;

SET @has_parent_id := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'travel_note_comment'
    AND column_name = 'parent_id'
);
SET @sql := IF(@has_parent_id = 0,
  'ALTER TABLE travel_note_comment ADD COLUMN parent_id BIGINT NULL AFTER user_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_parent_idx := (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'travel_note_comment'
    AND index_name = 'idx_comment_parent'
);
SET @sql := IF(@has_parent_idx = 0,
  'ALTER TABLE travel_note_comment ADD INDEX idx_comment_parent (parent_id)',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
