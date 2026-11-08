-- Add the fields introduced after the original V1 schema.
IF COL_LENGTH('dbo.categories', 'code') IS NULL
    ALTER TABLE dbo.categories ADD code VARCHAR(50) NULL;
GO
IF COL_LENGTH('dbo.categories', 'display_order') IS NULL
    ALTER TABLE dbo.categories ADD display_order INT NOT NULL CONSTRAINT DF_categories_display_order DEFAULT 0;
GO
IF COL_LENGTH('dbo.categories', 'is_deleted') IS NULL
    ALTER TABLE dbo.categories ADD is_deleted BIT NOT NULL CONSTRAINT DF_categories_is_deleted DEFAULT 0;
GO
IF COL_LENGTH('dbo.products', 'code') IS NULL
    ALTER TABLE dbo.products ADD code VARCHAR(50) NULL;
GO
IF COL_LENGTH('dbo.products', 'product_type') IS NULL
    ALTER TABLE dbo.products ADD product_type VARCHAR(30) NOT NULL CONSTRAINT DF_products_product_type DEFAULT 'FRUIT';
GO
IF COL_LENGTH('dbo.products', 'featured') IS NULL
    ALTER TABLE dbo.products ADD featured BIT NOT NULL CONSTRAINT DF_products_featured DEFAULT 0;
GO
IF COL_LENGTH('dbo.products', 'active') IS NULL
    ALTER TABLE dbo.products ADD active BIT NOT NULL CONSTRAINT DF_products_active DEFAULT 1;
GO
IF COL_LENGTH('dbo.products', 'is_deleted') IS NULL
    ALTER TABLE dbo.products ADD is_deleted BIT NOT NULL CONSTRAINT DF_products_is_deleted DEFAULT 0;
GO

-- Give pre-existing rows stable codes before adding unique indexes.
UPDATE dbo.categories SET code = CONCAT('LEGACY_CATEGORY_', id) WHERE code IS NULL;
UPDATE dbo.products SET code = CONCAT('LEGACY_PRODUCT_', id) WHERE code IS NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.categories') AND name = 'UX_categories_code')
    CREATE UNIQUE INDEX UX_categories_code ON dbo.categories(code);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.products') AND name = 'UX_products_code')
    CREATE UNIQUE INDEX UX_products_code ON dbo.products(code);
GO

-- Categories required by the sample products.
IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE code = 'CAT_IMPORT')
    INSERT INTO dbo.categories (name, code, description, image_url, display_order, active, created_at, is_deleted)
    VALUES (N'Trái cây nhập khẩu', 'CAT_IMPORT', N'Trái cây nhập khẩu tươi ngon.',
            'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=600&q=80', 1, 1, GETDATE(), 0);
IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE code = 'CAT_LOCAL')
    INSERT INTO dbo.categories (name, code, description, image_url, display_order, active, created_at, is_deleted)
    VALUES (N'Trái cây Việt Nam', 'CAT_LOCAL', N'Đặc sản trái cây tươi trong nước.',
            'https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=600&q=80', 2, 1, GETDATE(), 0);
IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE code = 'CAT_GIFT')
    INSERT INTO dbo.categories (name, code, description, image_url, display_order, active, created_at, is_deleted)
    VALUES (N'Giỏ quà trái cây', 'CAT_GIFT', N'Giỏ quà trái cây cho những dịp đặc biệt.',
            'https://images.unsplash.com/photo-1543083477-4f785aeafaa9?auto=format&fit=crop&w=600&q=80', 3, 1, GETDATE(), 0);
IF NOT EXISTS (SELECT 1 FROM dbo.categories WHERE code = 'CAT_DRIED')
    INSERT INTO dbo.categories (name, code, description, image_url, display_order, active, created_at, is_deleted)
    VALUES (N'Trái cây sấy', 'CAT_DRIED', N'Trái cây sấy thơm ngon.',
            'https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&w=600&q=80', 4, 1, GETDATE(), 0);
GO

-- Demo password is 123456. Hash generated with the application's BCryptPasswordEncoder.
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'ROLE_ADMIN')
    INSERT INTO dbo.roles (name, description) VALUES ('ROLE_ADMIN', N'Quản trị viên');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'ROLE_STAFF')
    INSERT INTO dbo.roles (name, description) VALUES ('ROLE_STAFF', N'Nhân viên');
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE name = 'ROLE_CUSTOMER')
    INSERT INTO dbo.roles (name, description) VALUES ('ROLE_CUSTOMER', N'Khách hàng');
GO

DECLARE @demo_password VARCHAR(100) = '$2a$10$XHaOfX5OK22m9R46qXKrHOxMkAIDdrG7AYsC5F.pMAQG.2PUAigIi';

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'admin.demo@fruitstore.com')
    INSERT INTO dbo.users (full_name, email, password, phone_number, address, enabled, created_at, is_deleted)
    VALUES (N'Demo Administrator', 'admin.demo@fruitstore.com', @demo_password, '0988111222', N'Hà Nội', 1, GETDATE(), 0);
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'staff.demo@fruitstore.com')
    INSERT INTO dbo.users (full_name, email, password, phone_number, address, enabled, created_at, is_deleted)
    VALUES (N'Demo Staff', 'staff.demo@fruitstore.com', @demo_password, '0977222333', N'Hà Nội', 1, GETDATE(), 0);
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'customer.demo@fruitstore.com')
    INSERT INTO dbo.users (full_name, email, password, phone_number, address, enabled, created_at, is_deleted)
    VALUES (N'Demo Customer', 'customer.demo@fruitstore.com', @demo_password, '0912345678', N'Hà Nội', 1, GETDATE(), 0);
GO

DECLARE @admin_id BIGINT = (SELECT id FROM dbo.users WHERE email = 'admin.demo@fruitstore.com');
DECLARE @staff_id BIGINT = (SELECT id FROM dbo.users WHERE email = 'staff.demo@fruitstore.com');
DECLARE @customer_id BIGINT = (SELECT id FROM dbo.users WHERE email = 'customer.demo@fruitstore.com');
DECLARE @admin_role_id BIGINT = (SELECT id FROM dbo.roles WHERE name = 'ROLE_ADMIN');
DECLARE @staff_role_id BIGINT = (SELECT id FROM dbo.roles WHERE name = 'ROLE_STAFF');
DECLARE @customer_role_id BIGINT = (SELECT id FROM dbo.roles WHERE name = 'ROLE_CUSTOMER');

IF NOT EXISTS (SELECT 1 FROM dbo.user_roles WHERE user_id = @admin_id AND role_id = @admin_role_id)
    INSERT INTO dbo.user_roles (user_id, role_id) VALUES (@admin_id, @admin_role_id);
IF NOT EXISTS (SELECT 1 FROM dbo.user_roles WHERE user_id = @admin_id AND role_id = @staff_role_id)
    INSERT INTO dbo.user_roles (user_id, role_id) VALUES (@admin_id, @staff_role_id);
IF NOT EXISTS (SELECT 1 FROM dbo.user_roles WHERE user_id = @staff_id AND role_id = @staff_role_id)
    INSERT INTO dbo.user_roles (user_id, role_id) VALUES (@staff_id, @staff_role_id);
IF NOT EXISTS (SELECT 1 FROM dbo.user_roles WHERE user_id = @customer_id AND role_id = @customer_role_id)
    INSERT INTO dbo.user_roles (user_id, role_id) VALUES (@customer_id, @customer_role_id);
GO

DECLARE @import_category_id BIGINT = (SELECT id FROM dbo.categories WHERE code = 'CAT_IMPORT');
DECLARE @local_category_id BIGINT = (SELECT id FROM dbo.categories WHERE code = 'CAT_LOCAL');
DECLARE @gift_category_id BIGINT = (SELECT id FROM dbo.categories WHERE code = 'CAT_GIFT');
DECLARE @dried_category_id BIGINT = (SELECT id FROM dbo.categories WHERE code = 'CAT_DRIED');

IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE code = 'FRUIT_ENVY_NZ')
    INSERT INTO dbo.products (name, code, category_id, origin, price, stock_quantity, unit, description, image_url, product_type, featured, active, created_at, is_deleted)
    VALUES (N'Táo Envy New Zealand', 'FRUIT_ENVY_NZ', @import_category_id, N'New Zealand', 220000, 85, N'kg', N'Táo Envy giòn ngọt, nhập khẩu tươi ngon.',
            'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80', 'FRUIT', 1, 1, GETDATE(), 0);
IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE code = 'FRUIT_MUSCAT_JP')
    INSERT INTO dbo.products (name, code, category_id, origin, price, stock_quantity, unit, description, image_url, product_type, featured, active, created_at, is_deleted)
    VALUES (N'Nho mẫu đơn Shine Muscat', 'FRUIT_MUSCAT_JP', @import_category_id, N'Nhật Bản', 850000, 30, N'hộp', N'Nho xanh không hạt, thơm ngọt.',
            'https://images.unsplash.com/photo-1596363505729-4190a9506133?auto=format&fit=crop&w=600&q=80', 'FRUIT', 1, 1, GETDATE(), 0);
IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE code = 'FRUIT_XOAI_HL')
    INSERT INTO dbo.products (name, code, category_id, origin, price, stock_quantity, unit, description, image_url, product_type, featured, active, created_at, is_deleted)
    VALUES (N'Xoài cát Hòa Lộc', 'FRUIT_XOAI_HL', @local_category_id, N'Việt Nam', 110000, 65, N'kg', N'Xoài chín thơm, thịt dày và ngọt.',
            'https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=600&q=80', 'FRUIT', 1, 1, GETDATE(), 0);
IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE code = 'GIFT_NGUPHUC_01')
    INSERT INTO dbo.products (name, code, category_id, origin, price, stock_quantity, unit, description, image_url, product_type, featured, active, created_at, is_deleted)
    VALUES (N'Giỏ trái cây Ngũ Phúc', 'GIFT_NGUPHUC_01', @gift_category_id, N'Việt Nam / Nhập khẩu', 1250000, 15, N'giỏ', N'Giỏ quà trái cây tươi, phù hợp làm quà tặng.',
            'https://images.unsplash.com/photo-1543083477-4f785aeafaa9?auto=format&fit=crop&w=600&q=80', 'GIFT_BASKET', 1, 1, GETDATE(), 0);
IF NOT EXISTS (SELECT 1 FROM dbo.products WHERE code = 'DRIED_XOAI_DEO')
    INSERT INTO dbo.products (name, code, category_id, origin, price, stock_quantity, unit, description, image_url, product_type, featured, active, created_at, is_deleted)
    VALUES (N'Xoài sấy dẻo', 'DRIED_XOAI_DEO', @dried_category_id, N'Việt Nam', 75000, 150, N'gói', N'Xoài sấy dẻo thơm ngon, không chất bảo quản.',
            'https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&w=600&q=80', 'DRIED_FRUIT', 0, 1, GETDATE(), 0);
GO
