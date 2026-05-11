-- ─────────────────────────────────────────────────────────────
-- Farmer Assistant – MySQL Initialization Script
-- Runs once when the Docker MySQL container first starts.
-- Spring Boot JPA will create/update tables via ddl-auto=update
-- ─────────────────────────────────────────────────────────────

CREATE DATABASE IF NOT EXISTS farmer_assistant_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE farmer_assistant_db;

-- Grant privileges to the application user
GRANT ALL PRIVILEGES ON farmer_assistant_db.* TO 'farmer_user'@'%';
FLUSH PRIVILEGES;
