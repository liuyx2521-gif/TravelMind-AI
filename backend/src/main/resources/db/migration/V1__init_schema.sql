CREATE TABLE IF NOT EXISTS user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  email VARCHAR(128) NOT NULL UNIQUE,
  avatar VARCHAR(512),
  password VARCHAR(255) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS attraction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  province VARCHAR(64),
  city VARCHAR(64) NOT NULL,
  description TEXT,
  cover_image VARCHAR(512),
  latitude DECIMAL(10,7),
  longitude DECIMAL(10,7),
  price DECIMAL(10,2) DEFAULT 0,
  best_season VARCHAR(64),
  open_time VARCHAR(128),
  score DECIMAL(3,1) DEFAULT 0,
  tags VARCHAR(255),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  INDEX idx_attraction_city (city),
  INDEX idx_attraction_score (score)
);

CREATE TABLE IF NOT EXISTS check_point (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  attraction_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL,
  description TEXT,
  photo VARCHAR(512),
  location VARCHAR(255),
  best_time VARCHAR(64),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  INDEX idx_checkpoint_attraction (attraction_id)
);

CREATE TABLE IF NOT EXISTS hotel (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  city VARCHAR(64) NOT NULL,
  address VARCHAR(255),
  price DECIMAL(10,2) DEFAULT 0,
  score DECIMAL(3,1) DEFAULT 0,
  cover VARCHAR(512),
  longitude DECIMAL(10,7),
  latitude DECIMAL(10,7),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  INDEX idx_hotel_city (city),
  INDEX idx_hotel_price (price),
  INDEX idx_hotel_score (score)
);

CREATE TABLE IF NOT EXISTS travel_note (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  content LONGTEXT NOT NULL,
  cover VARCHAR(512),
  images LONGTEXT,
  view_count INT DEFAULT 0,
  like_count INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  INDEX idx_note_user_time (user_id, create_time)
);

CREATE TABLE IF NOT EXISTS travel_note_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  note_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  parent_id BIGINT,
  username VARCHAR(64),
  avatar VARCHAR(1024),
  content VARCHAR(1000) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_comment_note (note_id),
  INDEX idx_comment_parent (parent_id)
);

CREATE TABLE IF NOT EXISTS favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  target_id BIGINT NOT NULL,
  target_type VARCHAR(32) NOT NULL,
  title VARCHAR(128),
  cover VARCHAR(1024),
  path VARCHAR(255),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_favorite (user_id, target_id, target_type)
);

CREATE TABLE IF NOT EXISTS browse_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  target_id BIGINT NOT NULL,
  target_type VARCHAR(32) NOT NULL,
  title VARCHAR(128),
  cover VARCHAR(1024),
  path VARCHAR(255),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_history (user_id, target_id, target_type),
  INDEX idx_history_user_time (user_id, create_time)
);

CREATE TABLE IF NOT EXISTS travel_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  budget DECIMAL(10,2),
  days INT,
  destination VARCHAR(128),
  season VARCHAR(64),
  content LONGTEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  INDEX idx_plan_user_time (user_id, create_time)
);

CREATE TABLE IF NOT EXISTS ai_conversation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(128),
  summary LONGTEXT,
  summary_message_count INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT DEFAULT 0,
  INDEX idx_ai_conversation_user_time (user_id, update_time)
);

CREATE TABLE IF NOT EXISTS ai_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  conversation_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(16) NOT NULL,
  content LONGTEXT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_ai_message_conversation_time (conversation_id, create_time)
);
