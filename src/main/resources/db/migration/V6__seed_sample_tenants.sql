-- Seed a sample tenant for testing and demo purposes
INSERT INTO tenants (name, schema_name) VALUES
('TechCorp', 'tech_corp_schema'),
('FinTrade Inc', 'fintrade_schema');

-- Seed sample projects for each tenant
INSERT INTO projects (tenant_id, name, description) VALUES
(1, 'Trading Platform Tests', 'Test suite for trading platform features'),
(1, 'Mobile App Tests', 'Test suite for mobile application'),
(2, 'Core Banking Tests', 'Test suite for core banking operations'),
(2, 'Risk Management Tests', 'Test suite for risk management module');
