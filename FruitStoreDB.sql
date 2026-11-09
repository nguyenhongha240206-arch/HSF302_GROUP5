-- =====================================================================
-- DATABASE SCRIPT: FruitStoreDB (Microsoft SQL Server)
-- PROJECT: Fruit Store Management System (Spring Boot + Thymeleaf)
-- COURSE: HSF302 - Group 5 (HaNH, HuuMT, HungPD, MinhPD, QuyBH, BacTX)
-- =====================================================================

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'FruitStoreDB')
BEGIN
    CREATE DATABASE FruitStoreDB;
END
GO

USE FruitStoreDB;
GO

-- 1. ROLES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'roles')
BEGIN
    CREATE TABLE roles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(50) NOT NULL UNIQUE,
        description NVARCHAR(255) NULL
    );
END
GO

-- 2. USERS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'users')
BEGIN
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        full_name NVARCHAR(100) NOT NULL,
        email NVARCHAR(150) NOT NULL UNIQUE,
        password NVARCHAR(255) NOT NULL,
        phone_number NVARCHAR(20) NULL,
        address NVARCHAR(255) NULL,
        avatar_url NVARCHAR(500) NULL,
        enabled BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(100) NULL,
        updated_by NVARCHAR(100) NULL,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2 NULL
    );
END
GO

-- 3. USER_ROLES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user_roles')
BEGIN
    CREATE TABLE user_roles (
        user_id BIGINT NOT NULL,
        role_id BIGINT NOT NULL,
        PRIMARY KEY (user_id, role_id),
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
    );
END
GO

-- 4. CATEGORIES TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'categories')
BEGIN
    CREATE TABLE categories (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(100) NOT NULL,
        code NVARCHAR(50) NOT NULL UNIQUE,
        description NVARCHAR(500) NULL,
        image_url NVARCHAR(500) NULL,
        display_order INT NOT NULL DEFAULT 0,
        active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(100) NULL,
        updated_by NVARCHAR(100) NULL,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2 NULL
    );
END
GO

-- 5. PRODUCTS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'products')
BEGIN
    CREATE TABLE products (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(150) NOT NULL,
        code NVARCHAR(50) NOT NULL UNIQUE,
        description NVARCHAR(MAX) NULL,
        price DECIMAL(15,2) NOT NULL,
        original_price DECIMAL(15,2) NULL,
        stock_quantity INT NOT NULL DEFAULT 0,
        unit NVARCHAR(30) NOT NULL,
        origin NVARCHAR(100) NULL,
        image_url NVARCHAR(500) NULL,
        product_type NVARCHAR(30) NOT NULL DEFAULT 'FRUIT',
        category_id BIGINT NOT NULL,
        featured BIT NOT NULL DEFAULT 0,
        active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(100) NULL,
        updated_by NVARCHAR(100) NULL,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2 NULL,
        FOREIGN KEY (category_id) REFERENCES categories(id)
    );
END
GO

-- 6. VOUCHERS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'vouchers')
BEGIN
    CREATE TABLE vouchers (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        code NVARCHAR(50) NOT NULL UNIQUE,
        name NVARCHAR(150) NOT NULL,
        discount_percent INT NOT NULL,
        max_discount_amount DECIMAL(15,2) NULL,
        min_order_amount DECIMAL(15,2) NULL,
        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        usage_limit INT NOT NULL DEFAULT 100,
        used_count INT NOT NULL DEFAULT 0,
        active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(100) NULL,
        updated_by NVARCHAR(100) NULL,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2 NULL
    );
END
GO

-- 7. ORDERS TABLE (UC-11: Cung cấp chọn khung giờ giao và ghi chú đóng gói quả)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'orders')
BEGIN
    CREATE TABLE orders (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_number NVARCHAR(50) NOT NULL UNIQUE,
        user_id BIGINT NULL,
        receiver_name NVARCHAR(100) NOT NULL,
        receiver_phone NVARCHAR(20) NOT NULL,
        shipping_address NVARCHAR(300) NOT NULL,
        note NVARCHAR(500) NULL,
        total_amount DECIMAL(15,2) NOT NULL,
        discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
        final_amount DECIMAL(15,2) NOT NULL,
        voucher_code NVARCHAR(50) NULL,
        delivery_time_type NVARCHAR(50) NULL, -- EXPRESS_2H / SCHEDULED
        delivery_time_slot NVARCHAR(100) NULL, -- Khung giờ nhận hàng
        packaging_requirement NVARCHAR(200) NULL, -- Yêu cầu đóng gói (Quả chín ăn ngay/Quả cứng thắp hương...)
        order_status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
        payment_method NVARCHAR(30) NOT NULL DEFAULT 'COD',
        payment_status NVARCHAR(30) NOT NULL DEFAULT 'UNPAID',
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(100) NULL,
        updated_by NVARCHAR(100) NULL,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2 NULL,
        FOREIGN KEY (user_id) REFERENCES users(id)
    );
END
GO

-- 8. ORDER_DETAILS TABLE (Hỗ trợ cả sản phẩm thường và UC-09: Giỏ quà tùy biến theo yêu cầu)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'order_details')
BEGIN
    CREATE TABLE order_details (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        product_id BIGINT NULL, -- Nullable khi là Giỏ quà tùy biến kết hợp nhiều loại quả
        product_name NVARCHAR(150) NOT NULL,
        item_details NVARCHAR(1000) NULL, -- Chi tiết đóng gói: Kiểu giỏ, nơ, thiệp mừng, thành phần hoa quả
        unit_price DECIMAL(15,2) NOT NULL,
        quantity INT NOT NULL,
        sub_total DECIMAL(15,2) NOT NULL,
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
        FOREIGN KEY (product_id) REFERENCES products(id)
    );
END
GO

-- 9. LOSS_SLIPS TABLE (Phiếu hao hụt / xuất hủy hoa quả)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'loss_slips')
BEGIN
    CREATE TABLE loss_slips (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        slip_code NVARCHAR(50) NOT NULL UNIQUE,
        slip_date DATETIME2 NOT NULL DEFAULT GETDATE(),
        note NVARCHAR(500) NULL,
        total_loss_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(100) NULL,
        updated_by NVARCHAR(100) NULL,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2 NULL
    );
END
GO

-- 10. LOSS_SLIP_DETAILS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'loss_slip_details')
BEGIN
    CREATE TABLE loss_slip_details (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        loss_slip_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        quantity INT NOT NULL,
        cost_price DECIMAL(15,2) NOT NULL,
        loss_reason NVARCHAR(30) NOT NULL DEFAULT 'SPOILED',
        note NVARCHAR(255) NULL,
        FOREIGN KEY (loss_slip_id) REFERENCES loss_slips(id) ON DELETE CASCADE,
        FOREIGN KEY (product_id) REFERENCES products(id)
    );
END
GO

-- 11. REVIEWS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'reviews')
BEGIN
    CREATE TABLE reviews (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        rating INT NOT NULL,
        comment NVARCHAR(MAX) NULL,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(100) NULL,
        updated_by NVARCHAR(100) NULL,
        is_deleted BIT NOT NULL DEFAULT 0,
        deleted_at DATETIME2 NULL,
        FOREIGN KEY (user_id) REFERENCES users(id),
        FOREIGN KEY (product_id) REFERENCES products(id)
    );
END
GO

PRINT N'>>> Cơ sở dữ liệu FruitStoreDB và các bảng đã được khởi tạo thành công!';
GO

-- =====================================================================
-- 12. SEED DATA (DỮ LIỆU MẪU BAN ĐẦU)
-- =====================================================================

-- 12.1 ROLES
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN')
    INSERT INTO roles (name, description) VALUES ('ROLE_ADMIN', N'Quản trị viên toàn quyền');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_STAFF')
    INSERT INTO roles (name, description) VALUES ('ROLE_STAFF', N'Nhân viên quản lý cửa hàng');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_CUSTOMER')
    INSERT INTO roles (name, description) VALUES ('ROLE_CUSTOMER', N'Khách hàng');
GO

-- 12.2 USERS (Mật khẩu mặc định: 123456 - mã hóa BCrypt)
-- Hash BCrypt của 123456: $2a$10$eofwoEwf2sr85ecHKeMDt.P.IjoDeOPi2REA7aHdpzyyxqlgRmhZC
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@fruitstore.com')
BEGIN
    INSERT INTO users (full_name, email, password, phone_number, address, avatar_url, enabled, created_at, is_deleted)
    VALUES (N'Nguyễn Quản Trị', 'admin@fruitstore.com', '$2a$10$eofwoEwf2sr85ecHKeMDt.P.IjoDeOPi2REA7aHdpzyyxqlgRmhZC', '0988111222', N'FPT University, Khu CNC Hòa Lạc, Hà Nội', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80', 1, GETDATE(), 0);
    
    DECLARE @adminId BIGINT = SCOPE_IDENTITY();
    DECLARE @adminRoleId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');
    DECLARE @staffRoleId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_STAFF');
    INSERT INTO user_roles (user_id, role_id) VALUES (@adminId, @adminRoleId), (@adminId, @staffRoleId);
END
GO

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'staff@fruitstore.com')
BEGIN
    INSERT INTO users (full_name, email, password, phone_number, address, avatar_url, enabled, created_at, is_deleted)
    VALUES (N'Trần Nhân Viên', 'staff@fruitstore.com', '$2a$10$eofwoEwf2sr85ecHKeMDt.P.IjoDeOPi2REA7aHdpzyyxqlgRmhZC', '0977222333', N'Thạch Thất, Hà Nội', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80', 1, GETDATE(), 0);
    
    DECLARE @staffUserId BIGINT = SCOPE_IDENTITY();
    DECLARE @staffRoleId2 BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_STAFF');
    INSERT INTO user_roles (user_id, role_id) VALUES (@staffUserId, @staffRoleId2);
END
GO

IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'customer@fruitstore.com')
BEGIN
    INSERT INTO users (full_name, email, password, phone_number, address, avatar_url, enabled, created_at, is_deleted)
    VALUES (N'Nguyễn Hồng Hà', 'customer@fruitstore.com', '$2a$10$eofwoEwf2sr85ecHKeMDt.P.IjoDeOPi2REA7aHdpzyyxqlgRmhZC', '0912345678', N'Cầu Giấy, Hà Nội', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80', 1, GETDATE(), 0);
    
    DECLARE @custUserId BIGINT = SCOPE_IDENTITY();
    DECLARE @custRoleId BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_CUSTOMER');
    INSERT INTO user_roles (user_id, role_id) VALUES (@custUserId, @custRoleId);
END
GO

-- 12.3 CATEGORIES
IF NOT EXISTS (SELECT 1 FROM categories WHERE code = 'CAT_IMPORT')
    INSERT INTO categories (name, code, description, image_url, display_order, active, created_at, is_deleted)
    VALUES (N'Trái cây nhập khẩu', 'CAT_IMPORT', N'Các loại trái cây cao cấp nhập khẩu đường hàng không từ Mỹ, Úc, New Zealand, Nhật, Hàn', 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=600&q=80', 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM categories WHERE code = 'CAT_LOCAL')
    INSERT INTO categories (name, code, description, image_url, display_order, active, created_at, is_deleted)
    VALUES (N'Trái cây đặc sản Việt', 'CAT_LOCAL', N'Hoa quả đặc sản ba miền Việt Nam đạt chuẩn VietGAP, GlobalGAP tươi ngon', 'https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=600&q=80', 2, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM categories WHERE code = 'CAT_GIFT')
    INSERT INTO categories (name, code, description, image_url, display_order, active, created_at, is_deleted)
    VALUES (N'Giỏ quà trái cây cao cấp', 'CAT_GIFT', N'Giỏ quà hoa quả trang trọng kèm nơ lụa và hoa tươi thích hợp biếu tặng, chúc mừng, dạm ngõ', 'https://images.unsplash.com/photo-1543083477-4f785aeafaa9?auto=format&fit=crop&w=600&q=80', 3, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM categories WHERE code = 'CAT_DRIED')
    INSERT INTO categories (name, code, description, image_url, display_order, active, created_at, is_deleted)
    VALUES (N'Trái cây sấy khô & Chế biến', 'CAT_DRIED', N'Hoa quả sấy dẻo, sấy thăng hoa giữ nguyên hương vị và dinh dưỡng tự nhiên', 'https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&w=600&q=80', 4, 1, GETDATE(), 0);
GO

-- 12.4 PRODUCTS (HOA QUẢ & SẢN PHẨM MẪU)
DECLARE @catImportId BIGINT = (SELECT id FROM categories WHERE code = 'CAT_IMPORT');
DECLARE @catLocalId BIGINT = (SELECT id FROM categories WHERE code = 'CAT_LOCAL');
DECLARE @catGiftId BIGINT = (SELECT id FROM categories WHERE code = 'CAT_GIFT');
DECLARE @catDriedId BIGINT = (SELECT id FROM categories WHERE code = 'CAT_DRIED');

-- Nhập khẩu
IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_ENVY_NZ')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Táo Envy New Zealand Size 70', 'FRUIT_ENVY_NZ', N'Táo Envy New Zealand nhập khẩu chính ngạch, thịt giòn tan, vị ngọt đậm đà và hương thơm ngát tự nhiên.', 220000, 250000, 85, N'kg', N'New Zealand', 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catImportId, 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_MUSCAT_JP')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Nho mẫu đơn Shine Muscat Nhật', 'FRUIT_MUSCAT_JP', N'Nho mẫu đơn Shine Muscat chuẩn hàng bay xách tay, quả to bóng mịn, không hạt, thơm hương sữa ngào ngạt.', 850000, 950000, 30, N'hộp', N'Nhật Bản', 'https://images.unsplash.com/photo-1596363505729-4190a9506133?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catImportId, 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_FINGER_AU')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Nho ngón tay Midnight Beauty Úc', 'FRUIT_FINGER_AU', N'Nho đen không hạt ngón tay Úc hình dáng thon dài độc đáo, thịt quả giòn rụm, vị ngọt thanh đậm đà.', 260000, 290000, 45, N'kg', N'Úc', 'https://images.unsplash.com/photo-1537640538966-79f369143f8f?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catImportId, 0, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_CARA_AU')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Cam Cara Cara ruột đỏ Úc', 'FRUIT_CARA_AU', N'Cam không hạt ruột đỏ Cara ruột đỏ mọng nước, hàm lượng vitamin C cực cao, ngọt thanh mát.', 135000, 160000, 120, N'kg', N'Úc', 'https://images.unsplash.com/photo-1582979512210-99b6a53386f9?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catImportId, 0, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_NAVEL_AU')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Cam vàng Navel Úc không hạt', 'FRUIT_NAVEL_AU', N'Cam vàng Navel vỏ mỏng dễ bóc, múi cam vàng mọng nước, thơm lừng vị ngọt tươi mát.', 115000, 135000, 95, N'kg', N'Úc', 'https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catImportId, 0, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_CHERRY_US')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Cherry đỏ Mỹ Size 8.5 chuẩn bay', 'FRUIT_CHERRY_US', N'Cherry Washington đỏ thẫm cuống xanh tươi rói, quả chắc nịch, thịt ngọt đậm giòn tan đẳng cấp trái cây thượng hạng.', 490000, 560000, 40, N'hộp', N'Mỹ', 'https://images.unsplash.com/photo-1528825871115-3581a5387919?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catImportId, 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_STRAW_KR')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Dâu tây Hàn Quốc Seolhyang cao cấp', 'FRUIT_STRAW_KR', N'Dâu giống Seolhyang nổi tiếng xứ kim chi, quả đỏ mọng hình nón hoàn hảo, vị ngọt ngào và mùi thơm quyến rũ đặc trưng.', 350000, 390000, 25, N'hộp', N'Hàn Quốc', 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catImportId, 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_KIWI_NZ')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Kiwi vàng Zespri SunGold New Zealand', 'FRUIT_KIWI_NZ', N'Kiwi vàng Zespri ruột vàng ươm mọng nước, giàu vitamin C gấp 3 lần cam, vị chua ngọt cân bằng tinh tế.', 210000, 240000, 70, N'kg', N'New Zealand', 'https://images.unsplash.com/photo-1585059895524-72359e06133a?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catImportId, 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_PEAR_KR')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Lê nâu Hàn Quốc Singold quả to giòn', 'FRUIT_PEAR_KR', N'Lê nâu Hàn Quốc chuẩn xuất khẩu quả to tròn đều, vỏ nâu mỏng tang, thịt trắng giòn nhiều nước ngọt thanh tao.', 145000, 170000, 60, N'kg', N'Hàn Quốc', 'https://images.unsplash.com/photo-1615484477778-ca3b77940c25?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catImportId, 0, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_BLUEBERRY_US')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Việt quất tươi hữu cơ Driscoll''s Mỹ', 'FRUIT_BLUEBERRY_US', N'Việt quất quả chắc mẩy phủ phấn trắng tự nhiên, giàu chất chống oxy hóa anthocyanin tốt cho sức khỏe và sắc đẹp.', 165000, 195000, 50, N'hộp', N'Mỹ', 'https://images.unsplash.com/photo-1498557850523-fd3d118b962e?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catImportId, 0, 1, GETDATE(), 0);

-- Đặc sản Việt Nam
IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_XOAI_HL')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Xoài Cát Hòa Lộc Tiền Giang loại 1', 'FRUIT_XOAI_HL', N'Xoài cát Hòa Lộc chính gốc Tiền Giang, thịt dày mịn, không xơ, hương thơm nồng nàn vị ngọt lịm.', 110000, 130000, 65, N'kg', N'Việt Nam', 'https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catLocalId, 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_BUOI_BT')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Bưởi da xanh Bến Tre ruột hồng', 'FRUIT_BUOI_BT', N'Bưởi da xanh loại 1 cành lá sum suê, tép bưởi mọng nước, giòn ráo không hạt vị chua ngọt cân bằng.', 85000, 95000, 50, N'quả', N'Việt Nam', 'https://images.unsplash.com/photo-1577234286642-fc512a5f8f11?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catLocalId, 0, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_SAURIENG_RI6')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Sầu riêng Ri6 cơm vàng hạt lép Đắk Lắk', 'FRUIT_SAURIENG_RI6', N'Sầu riêng Ri6 chín cây tự nhiên, cơm vàng óng ả dày cùi béo ngậy hạt lép, hương thơm quyến rũ nức mũi.', 160000, 185000, 35, N'kg', N'Việt Nam', 'https://images.unsplash.com/photo-1587132137056-bfbf0166836e?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catLocalId, 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_DUALUOI_HL')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Dưa lưới Huỳnh Long ruột cam ngọt mát', 'FRUIT_DUALUOI_HL', N'Dưa lưới trồng nhà màng công nghệ cao, vân lưới dày đều đẹp, ruột cam giòn ngọt lịm hương thơm mát.', 95000, 115000, 55, N'kg', N'Việt Nam', 'https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catLocalId, 0, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_MANGCUT_LT')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Măng cụt Lái Thiêu chuẩn VietGAP', 'FRUIT_MANGCUT_LT', N'Nữ hoàng trái cây nhiệt đới Lái Thiêu vỏ mềm mỏng, múi trắng nõn mọng nước, vị chua ngọt thanh tao.', 125000, 150000, 40, N'kg', N'Việt Nam', 'https://images.unsplash.com/photo-1546548970-71785318a17b?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catLocalId, 0, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'FRUIT_THANHLONG_BT')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Thanh long ruột đỏ Bình Thuận xuất khẩu', 'FRUIT_THANHLONG_BT', N'Thanh long ruột đỏ đậm tự nhiên, ngọt đậm thơm ngon, giàu chất xơ và chất chống oxy hóa tự nhiên.', 45000, 55000, 80, N'kg', N'Việt Nam', 'https://images.unsplash.com/photo-1527324688151-0e627063f2b1?auto=format&fit=crop&w=600&q=80', 'FRUIT', @catLocalId, 0, 1, GETDATE(), 0);

-- Giỏ quà trái cây cao cấp
IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'GIFT_NGUPHUC_01')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Giỏ Trái Cây Ngũ Phúc Đoàn Viên', 'GIFT_NGUPHUC_01', N'Thiết kế giỏ mây tre đan cao cấp, kết hợp Táo Envy, Nho mẫu đơn, Cam Cara, Lê Hàn Quốc và Kiwi vàng. Kèm hoa lá tươi, ruy băng sang trọng.', 1250000, 1500000, 15, N'giỏ', N'Việt Nam / Nhập khẩu', 'https://images.unsplash.com/photo-1543083477-4f785aeafaa9?auto=format&fit=crop&w=600&q=80', 'GIFT_BASKET', @catGiftId, 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'GIFT_VIP_BIZ')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Hộp Quà Trái Cây Doanh Nghiệp VIP', 'GIFT_VIP_BIZ', N'Hộp quà nắp kính ép nhũ sang trọng, tuyển chọn Cherry Mỹ, Dâu tây Hàn Quốc, Nho xanh không hạt. Đẳng cấp quà biếu đối tác.', 1850000, 2100000, 8, N'hộp', N'Nhập khẩu', 'https://images.unsplash.com/photo-1528825871115-3581a5387919?auto=format&fit=crop&w=600&q=80', 'GIFT_BASKET', @catGiftId, 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'GIFT_DAMNGO_02')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Giỏ Quà Hoa Quả Dạm Ngõ Thắm Duyên', 'GIFT_DAMNGO_02', N'Mẫu giỏ quà cưới hỏi, dạm ngõ thiết kế thắt nơ lụa đỏ song hỷ, kết hợp nho đen không hạt, táo đỏ Ruby, cam vàng và dưa lưới.', 1600000, 1850000, 12, N'giỏ', N'Nhập khẩu & Đặc sản', 'https://images.unsplash.com/photo-1519996529931-28324d5a630e?auto=format&fit=crop&w=600&q=80', 'GIFT_BASKET', @catGiftId, 1, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'GIFT_LEPHAT_03')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Giỏ Quà Trái Cây Thắp Hương Lễ Phật', 'GIFT_LEPHAT_03', N'Tuyển chọn các quả tươi cứng cáp đều đẹp: Bưởi da xanh cành lá, Táo đỏ, Cam vàng, Xoài cát dâng hương ban thờ, tổ tiên, lễ chùa.', 750000, 880000, 20, N'giỏ', N'Nhập khẩu & Việt Nam', 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=600&q=80', 'GIFT_BASKET', @catGiftId, 0, 1, GETDATE(), 0);

-- Trái cây sấy khô & chế biến
IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'DRIED_XOAI_DEO')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Xoài sấy dẻo xuất khẩu loại 1', 'DRIED_XOAI_DEO', N'Xoài cát sấy dẻo công nghệ lạnh, không chất bảo quản, giữ nguyên vị chua ngọt tự nhiên dẻo thơm.', 75000, 90000, 150, N'gói', N'Việt Nam', 'https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&w=600&q=80', 'DRIED_FRUIT', @catDriedId, 0, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'DRIED_MIT_DALAT')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Mít sấy giòn nguyên miếng Dalat', 'DRIED_MIT_DALAT', N'Mít nghệ tươi sấy chân không giòn rụm, màu vàng óng tự nhiên, không đường hóa học, thơm lừng vị mít chín.', 60000, 75000, 130, N'gói', N'Việt Nam', 'https://images.unsplash.com/photo-1601039641847-7857b994d704?auto=format&fit=crop&w=600&q=80', 'DRIED_FRUIT', @catDriedId, 0, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'DRIED_STRAW_FREEZE')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Dâu tây sấy thăng hoa giữ trọn vitamin', 'DRIED_STRAW_FREEZE', N'Dâu tây Đà Lạt sấy thăng hoa công nghệ Nhật Bản, giữ nguyên hình dáng quả, vị chua ngọt giòn xốp tan trong miệng.', 115000, 135000, 80, N'hộp', N'Việt Nam', 'https://images.unsplash.com/photo-1518492104633-130d0cc84637?auto=format&fit=crop&w=600&q=80', 'DRIED_FRUIT', @catDriedId, 0, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM products WHERE code = 'DRIED_MACCA_DL')
    INSERT INTO products (name, code, description, price, original_price, stock_quantity, unit, origin, image_url, product_type, category_id, featured, active, created_at, is_deleted)
    VALUES (N'Hạt macca sấy nứt vỏ Đắk Lắk VIP', 'DRIED_MACCA_DL', N'Hạt macca loại 1 nứt vỏ tự nhiên kèm dụng cụ tách, nhân trắng ngà béo ngậy ngọt bùi dinh dưỡng cho cả gia đình.', 140000, 165000, 90, N'hộp', N'Việt Nam', 'https://images.unsplash.com/photo-1509358271058-acd22cc93898?auto=format&fit=crop&w=600&q=80', 'DRIED_FRUIT', @catDriedId, 0, 1, GETDATE(), 0);
GO

-- 12.5 VOUCHERS
IF NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'CHAOBANMOI')
    INSERT INTO vouchers (code, name, discount_percent, max_discount_amount, min_order_amount, start_date, end_date, usage_limit, used_count, active, created_at, is_deleted)
    VALUES ('CHAOBANMOI', N'Chào mừng bạn mới - Giảm ngay 10%', 10, 100000, 200000, DATEADD(day, -5, CAST(GETDATE() AS DATE)), DATEADD(month, 2, CAST(GETDATE() AS DATE)), 500, 12, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'FREESHIP')
    INSERT INTO vouchers (code, name, discount_percent, max_discount_amount, min_order_amount, start_date, end_date, usage_limit, used_count, active, created_at, is_deleted)
    VALUES ('FREESHIP', N'Ưu đãi giảm giá 15% cho đơn từ 500k', 15, 150000, 500000, DATEADD(day, -2, CAST(GETDATE() AS DATE)), DATEADD(month, 1, CAST(GETDATE() AS DATE)), 200, 5, 1, GETDATE(), 0);

IF NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'TRIAN50K')
    INSERT INTO vouchers (code, name, discount_percent, max_discount_amount, min_order_amount, start_date, end_date, usage_limit, used_count, active, created_at, is_deleted)
    VALUES ('TRIAN50K', N'Tri ân khách hàng thân thiết - Giảm 20%', 20, 50000, 250000, DATEADD(day, -10, CAST(GETDATE() AS DATE)), DATEADD(month, 3, CAST(GETDATE() AS DATE)), 300, 8, 1, GETDATE(), 0);
GO

PRINT N'>>> Dữ liệu mẫu (Seed Data) đã được chèn vào FruitStoreDB thành công!';
GO


