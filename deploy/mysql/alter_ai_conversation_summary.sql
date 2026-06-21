USE travelmind_ai;

SET @has_summary := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'ai_conversation'
    AND column_name = 'summary'
);
SET @sql := IF(@has_summary = 0,
  'ALTER TABLE ai_conversation ADD COLUMN summary LONGTEXT NULL',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_summary_count := (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'ai_conversation'
    AND column_name = 'summary_message_count'
);
SET @sql := IF(@has_summary_count = 0,
  'ALTER TABLE ai_conversation ADD COLUMN summary_message_count INT DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
