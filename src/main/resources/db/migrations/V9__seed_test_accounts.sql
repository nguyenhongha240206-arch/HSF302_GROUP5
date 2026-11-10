-- =====================================================================
-- DATABASE SCRIPT: V9__seed_test_accounts.sql (Microsoft SQL Server)
-- PROJECT: Fruit Store Management System (Spring Boot + Thymeleaf)
-- DESCRIPTION: Seed Verified Test Accounts (Admin & Customer with BCrypt Passwords)
-- =====================================================================

-- 1. Fix admin@fruitstore.vn password to valid BCrypt hash for 'Admin@123'
UPDATE users 
SET password = '$2a$10$7WiuhEoK9e5boiWT2cIDkuwrxARwj43Buk.0Jbtqj7yjOu2I7cfk6',
    enabled = 1,
    is_deleted = 0
WHERE email = 'admin@fruitstore.vn';
GO

-- 2. Seed Admin account: admin@fruitstore.com (Password: 123456)
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@fruitstore.com')
BEGIN
    INSERT INTO users (full_name, email, password, phone_number, address, avatar_url, enabled, created_at)
    VALUES (
        N'Quản Trị Viên (Admin)',
        'admin@fruitstore.com',
        '$2a$10$04gUPGMgw/9He4eJVAyZ0OjrIoukbhtfDqhrC4xJtOApyUi.p2Nvy',
        '0988111222',
        N'Số 1 Đại Cồ Việt, Hai Bà Trưng, Hà Nội',
        'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80',
        1,
        GETDATE()
    );

    DECLARE @adm_id BIGINT = (SELECT id FROM users WHERE email = 'admin@fruitstore.com');
    DECLARE @r_adm BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');

    IF (@r_adm IS NOT NULL) INSERT INTO user_roles (user_id, role_id) VALUES (@adm_id, @r_adm);
END
ELSE
BEGIN
    UPDATE users 
    SET password = '$2a$10$04gUPGMgw/9He4eJVAyZ0OjrIoukbhtfDqhrC4xJtOApyUi.p2Nvy',
        enabled = 1,
        is_deleted = 0
    WHERE email = 'admin@fruitstore.com';
END
GO

-- 3. Seed Customer account: customer@fruitstore.com (Password: 123456)
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer@fruitstore.com')
BEGIN
    INSERT INTO users (full_name, email, password, phone_number, address, avatar_url, enabled, created_at)
    VALUES (
        N'Nguyễn Văn Khách (Customer)',
        'customer@fruitstore.com',
        '$2a$10$04gUPGMgw/9He4eJVAyZ0OjrIoukbhtfDqhrC4xJtOApyUi.p2Nvy',
        '0933444555',
        N'Số 123 Đường Cầu Giấy, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội',
        'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80',
        1,
        GETDATE()
    );

    DECLARE @cust_id BIGINT = (SELECT id FROM users WHERE email = 'customer@fruitstore.com');
    DECLARE @r_cust BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_CUSTOMER');

    IF (@r_cust IS NOT NULL) INSERT INTO user_roles (user_id, role_id) VALUES (@cust_id, @r_cust);

    -- Seed sample addresses
    IF NOT EXISTS (SELECT 1 FROM user_addresses WHERE user_id = @cust_id)
    BEGIN
        INSERT INTO user_addresses (user_id, recipient_name, phone_number, detailed_address, is_default, created_at)
        VALUES
        (@cust_id, N'Nguyễn Văn Khách', '0933444555', N'Số 123 Đường Cầu Giấy, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội', 1, GETDATE()),
        (@cust_id, N'Nguyễn Văn Khách (Văn phòng)', '0933444555', N'Tòa nhà FPT, Phố Duy Tân, Dịch Vọng Hậu, Cầu Giấy, Hà Nội', 0, GETDATE());
    END
END
ELSE
BEGIN
    UPDATE users 
    SET password = '$2a$10$04gUPGMgw/9He4eJVAyZ0OjrIoukbhtfDqhrC4xJtOApyUi.p2Nvy',
        phone_number = '0933444555',
        enabled = 1,
        is_deleted = 0
    WHERE email = 'customer@fruitstore.com';
END
GO
