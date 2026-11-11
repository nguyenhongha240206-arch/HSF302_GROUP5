-- V10: Bổ sung cột deleted_at cho bảng banners và user_vouchers để đồng bộ với BaseEntity

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'banners')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('banners') AND name = 'deleted_at')
    BEGIN
        ALTER TABLE banners ADD deleted_at DATETIME2 NULL;
    END;
END;

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'user_vouchers')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('user_vouchers') AND name = 'deleted_at')
    BEGIN
        ALTER TABLE user_vouchers ADD deleted_at DATETIME2 NULL;
    END;
END;

