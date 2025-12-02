-- Users table and seed data

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL DEFAULT 'USER',
  display_name VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed hardcoded admin and user
INSERT INTO users (username, password, email, role, display_name)
VALUES 
  ('admin', 'admin', 'admin@testnext.com', 'ADMIN', 'System Administrator'),
  ('user', 'user', 'user@testnext.com', 'USER', 'Regular User')
ON CONFLICT (username) DO NOTHING;
