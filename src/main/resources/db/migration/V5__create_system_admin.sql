-- System admin user table for central management (dev profile with H2)
CREATE TABLE IF NOT EXISTS system_users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  hashed_password VARCHAR(255),
  display_name VARCHAR(255),
  role VARCHAR(50) NOT NULL DEFAULT 'SYSTEM_ADMIN',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed system admin user (password: admin123)
-- Hash generated using bcrypt: $2a$10$k/k/k/k/k/k/k/k/k/k/k/7eMpJSLPKMPxUX5UvUJt1QQjqaOFwu
INSERT INTO system_users (username, email, hashed_password, display_name, role) VALUES
('neera', 'neera@example.com', '$2a$10$k/k/k/k/k/k/k/k/k/k/k/7eMpJSLPKMPxUX5UvUJt1QQjqaOFwu', 'Neera Maurya', 'SYSTEM_ADMIN');
