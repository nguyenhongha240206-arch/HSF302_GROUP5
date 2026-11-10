-- =====================================================================
-- DATABASE SCRIPT: V8__address_and_otp.sql (Microsoft SQL Server)
-- PROJECT: Fruit Store Management System (Spring Boot + Thymeleaf)
-- DESCRIPTION: Add user_addresses and password_reset_otps tables
-- =====================================================================

-- 1. USER_ADDRESSES TABLE (Sổ địa chỉ nhận hàng)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user_addresses')
BEGIN
CREATE TABLE user_addresses (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipient_name NVARCHAR(100) NOT NULL,
    phone_number NVARCHAR(20) NOT NULL,
    detailed_address NVARCHAR(255) NOT NULL,
    is_default BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
END
GO

-- 2. PASSWORD_RESET_OTPS TABLE (Mã OTP xác thực quên mật khẩu qua Email)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'password_reset_otps')
BEGIN
CREATE TABLE password_reset_otps (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(150) NOT NULL,
    otp_code NVARCHAR(6) NOT NULL,
    expiry_time DATETIME2 NOT NULL,
    is_used BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
END
GO
