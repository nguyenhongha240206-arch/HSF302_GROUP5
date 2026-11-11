-- Migration V11: Create user_carts table and ensure NVARCHAR text types
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'user_carts')
BEGIN
    CREATE TABLE dbo.user_carts (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        cart_data_json NVARCHAR(MAX) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_user_carts_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE
    );
END
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.products') AND name = 'unit' AND system_type_id = 167)
BEGIN
    ALTER TABLE dbo.products ALTER COLUMN unit NVARCHAR(50) NULL;
END
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.products') AND name = 'image_url' AND system_type_id = 167)
BEGIN
    ALTER TABLE dbo.products ALTER COLUMN image_url NVARCHAR(1000) NULL;
END
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.reviews') AND name = 'created_by' AND system_type_id = 167)
BEGIN
    ALTER TABLE dbo.reviews ALTER COLUMN created_by NVARCHAR(100) NULL;
    ALTER TABLE dbo.reviews ALTER COLUMN updated_by NVARCHAR(100) NULL;
END
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.loss_slips') AND name = 'created_by' AND system_type_id = 167)
BEGIN
    ALTER TABLE dbo.loss_slips ALTER COLUMN created_by NVARCHAR(100) NULL;
    ALTER TABLE dbo.loss_slips ALTER COLUMN updated_by NVARCHAR(100) NULL;
END
GO

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.wishlist_items') AND name = 'created_by' AND system_type_id = 167)
BEGIN
    ALTER TABLE dbo.wishlist_items ALTER COLUMN created_by NVARCHAR(100) NULL;
    ALTER TABLE dbo.wishlist_items ALTER COLUMN updated_by NVARCHAR(100) NULL;
END
GO

