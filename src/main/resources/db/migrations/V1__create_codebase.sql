-- =====================================================================
-- DATABASE MIGRATION SCRIPT: V1__create_codebase.sql (Microsoft SQL Server)
-- PROJECT: Fruit Store Management System (Spring Boot + Thymeleaf)
-- COURSE: HSF302 - Group 5
-- =====================================================================

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

-- 7. ORDERS TABLE (Hỗ trợ chọn khung giờ giao hoa quả & ghi chú đóng gói)
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
        delivery_time_type NVARCHAR(50) NULL,
        delivery_time_slot NVARCHAR(100) NULL,
        packaging_requirement NVARCHAR(200) NULL,
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

-- 8. ORDER_DETAILS TABLE (Hỗ trợ sản phẩm thường & giỏ quà tùy biến theo yêu cầu)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'order_details')
BEGIN
    CREATE TABLE order_details (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        product_id BIGINT NULL,
        product_name NVARCHAR(150) NOT NULL,
        item_details NVARCHAR(1000) NULL,
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