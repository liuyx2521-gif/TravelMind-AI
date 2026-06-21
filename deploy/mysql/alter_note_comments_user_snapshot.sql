SET @has_username := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'travel_note_comment'
    AND column_name = 'username'
);
SET @sql := IF(@has_username = 0,
  'ALTER TABLE travel_note_comment ADD COLUMN username VARCHAR(64) NULL AFTER user_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_avatar := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'travel_note_comment'
    AND column_name = 'avatar'
);
SET @sql := IF(@has_avatar = 0,
  'ALTER TABLE travel_note_comment ADD COLUMN avatar VARCHAR(1024) NULL AFTER username',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
