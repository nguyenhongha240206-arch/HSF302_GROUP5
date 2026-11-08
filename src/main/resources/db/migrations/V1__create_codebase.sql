-- =====================================================================
-- DATABASE SCRIPT: FruitStoreDB (Microsoft SQL Server)
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
                            description NVARCHAR(500) NULL,
                            image_url NVARCHAR(500) NULL,
                            active BIT NOT NULL DEFAULT 1,
                            created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
                            updated_at DATETIME2 NULL
);
END
GO

-- 5. PRODUCTS (FRUITS) TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'products')
BEGIN
CREATE TABLE products (
                          id BIGINT IDENTITY(1,1) PRIMARY KEY,
                          name NVARCHAR(150) NOT NULL,
                          category_id BIGINT NOT NULL,
                          origin NVARCHAR(100) NULL,
                          price DECIMAL(18,2) NOT NULL,
                          stock_quantity INT NOT NULL DEFAULT 0,
                          unit NVARCHAR(30) NOT NULL CONSTRAINT DF_products_unit DEFAULT N'kg',
                          description NVARCHAR(MAX) NULL,
                          image_url NVARCHAR(500) NULL,
                          status NVARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
                          created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
                          updated_at DATETIME2 NULL,
                          FOREIGN KEY (category_id) REFERENCES categories(id)
);
END
GO

-- 6. BASKETS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'baskets')
BEGIN
CREATE TABLE baskets (
                         id BIGINT IDENTITY(1,1) PRIMARY KEY,
                         name NVARCHAR(150) NOT NULL,
                         type NVARCHAR(50) NOT NULL,
                         base_price DECIMAL(18,2) NOT NULL DEFAULT 0.0,
                         total_price DECIMAL(18,2) NOT NULL DEFAULT 0.0,
                         description NVARCHAR(MAX) NULL,
                         image_url NVARCHAR(500) NULL,
                         status NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                         created_by BIGINT NULL,
                         created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
                         updated_at DATETIME2 NULL,
                         FOREIGN KEY (created_by) REFERENCES users(id)
);
END
GO

-- 7. BASKET_ITEMS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'basket_items')
BEGIN
CREATE TABLE basket_items (
                              id BIGINT IDENTITY(1,1) PRIMARY KEY,
                              basket_id BIGINT NOT NULL,
                              product_id BIGINT NOT NULL,
                              quantity INT NOT NULL DEFAULT 1,
                              unit_price DECIMAL(18,2) NOT NULL,
                              subtotal DECIMAL(18,2) NOT NULL,
                              FOREIGN KEY (basket_id) REFERENCES baskets(id) ON DELETE CASCADE,
                              FOREIGN KEY (product_id) REFERENCES products(id)
);
END
GO

-- 8. ORDERS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'orders')
BEGIN
CREATE TABLE orders (
                        id BIGINT IDENTITY(1,1) PRIMARY KEY,
                        order_code NVARCHAR(50) NOT NULL UNIQUE,
                        user_id BIGINT NOT NULL,
                        customer_name NVARCHAR(100) NOT NULL,
                        customer_phone NVARCHAR(20) NOT NULL,
                        shipping_address NVARCHAR(255) NOT NULL,
                        total_amount DECIMAL(18,2) NOT NULL,
                        discount_amount DECIMAL(15,2) NULL CONSTRAINT DF_orders_discount_amount DEFAULT 0.0,
                        final_amount DECIMAL(18,2) NOT NULL,
                        order_status VARCHAR(30) NOT NULL CONSTRAINT DF_orders_order_status DEFAULT 'PENDING',
                        payment_status VARCHAR(30) NOT NULL CONSTRAINT DF_orders_payment_status DEFAULT 'UNPAID',
                        payment_method VARCHAR(30) NOT NULL CONSTRAINT DF_orders_payment_method DEFAULT 'COD',
                        note NVARCHAR(500) NULL,
                        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
                        updated_at DATETIME2 NULL,
                        FOREIGN KEY (user_id) REFERENCES users(id)
);
END
GO

-- Existing databases may have SQL Server-generated default constraint names.
-- Replace them before altering the columns so the type matches the JPA entities.
DECLARE @column_name SYSNAME;
DECLARE @existing_constraint SYSNAME;
DECLARE @table_name SYSNAME;
DECLARE @column_type NVARCHAR(100);
DECLARE @target_constraint SYSNAME;
DECLARE @default_value NVARCHAR(100);
DECLARE @alter_sql NVARCHAR(MAX);
DECLARE @column_fixes TABLE (
    table_name SYSNAME NOT NULL,
    column_name SYSNAME NOT NULL,
    column_type NVARCHAR(100) NOT NULL,
    constraint_name SYSNAME NOT NULL,
    default_value NVARCHAR(100) NOT NULL
);

INSERT INTO @column_fixes (table_name, column_name, column_type, constraint_name, default_value)
VALUES
    ('orders', 'discount_amount', 'DECIMAL(15,2) NULL', 'DF_orders_discount_amount', '0.0'),
    ('orders', 'order_status', 'VARCHAR(30) NOT NULL', 'DF_orders_order_status', '''PENDING'''),
    ('orders', 'payment_status', 'VARCHAR(30) NOT NULL', 'DF_orders_payment_status', '''UNPAID'''),
    ('orders', 'payment_method', 'VARCHAR(30) NOT NULL', 'DF_orders_payment_method', '''COD'''),
    ('products', 'unit', 'NVARCHAR(30) NOT NULL', 'DF_products_unit', 'N''kg''');

DECLARE column_fix_cursor CURSOR LOCAL FAST_FORWARD FOR
    SELECT table_name, column_name, column_type, constraint_name, default_value
    FROM @column_fixes;

OPEN column_fix_cursor;
FETCH NEXT FROM column_fix_cursor INTO @table_name, @column_name, @column_type, @target_constraint, @default_value;

WHILE @@FETCH_STATUS = 0
BEGIN
    IF OBJECT_ID(@table_name, 'U') IS NOT NULL
       AND COL_LENGTH(@table_name, @column_name) IS NOT NULL
    BEGIN
        SET @existing_constraint = NULL;
        SELECT @existing_constraint = dc.name
        FROM sys.default_constraints dc
        INNER JOIN sys.columns c ON c.object_id = dc.parent_object_id AND c.column_id = dc.parent_column_id
        WHERE dc.parent_object_id = OBJECT_ID(@table_name) AND c.name = @column_name;

        IF @existing_constraint IS NOT NULL
        BEGIN
            SET @alter_sql = N'ALTER TABLE ' + QUOTENAME(@table_name) + N' DROP CONSTRAINT ' + QUOTENAME(@existing_constraint);
            EXEC sys.sp_executesql @alter_sql;
        END;

        SET @alter_sql = N'ALTER TABLE ' + QUOTENAME(@table_name) + N' ALTER COLUMN ' + QUOTENAME(@column_name) + N' ' + @column_type;
        EXEC sys.sp_executesql @alter_sql;

        SET @alter_sql = N'ALTER TABLE ' + QUOTENAME(@table_name) + N' ADD CONSTRAINT ' + QUOTENAME(@target_constraint)
            + N' DEFAULT ' + @default_value + N' FOR ' + QUOTENAME(@column_name);
        EXEC sys.sp_executesql @alter_sql;
    END;

    FETCH NEXT FROM column_fix_cursor INTO @table_name, @column_name, @column_type, @target_constraint, @default_value;
END;

CLOSE column_fix_cursor;
DEALLOCATE column_fix_cursor;
GO

-- 9. ORDER_ITEMS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'order_items')
BEGIN
CREATE TABLE order_items (
                             id BIGINT IDENTITY(1,1) PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NULL,
                             basket_id BIGINT NULL,
                             item_type NVARCHAR(50) NOT NULL,
                             item_name NVARCHAR(150) NOT NULL,
                             quantity INT NOT NULL,
                             unit_price DECIMAL(18,2) NOT NULL,
                             subtotal DECIMAL(18,2) NOT NULL,
                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             FOREIGN KEY (product_id) REFERENCES products(id),
                             FOREIGN KEY (basket_id) REFERENCES baskets(id)
);
END
GO

-- 10. REVIEWS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'reviews')
BEGIN
CREATE TABLE reviews (
                         id BIGINT IDENTITY(1,1) PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         product_id BIGINT NULL,
                         basket_id BIGINT NULL,
                         rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         comment NVARCHAR(MAX) NULL,
                         created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
                         FOREIGN KEY (user_id) REFERENCES users(id),
                         FOREIGN KEY (product_id) REFERENCES products(id),
                         FOREIGN KEY (basket_id) REFERENCES baskets(id)
);
END
GO

-- 11. INVENTORY_LOGS TABLE
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'inventory_logs')
BEGIN
CREATE TABLE inventory_logs (
                                id BIGINT IDENTITY(1,1) PRIMARY KEY,
                                product_id BIGINT NOT NULL,
                                change_type NVARCHAR(50) NOT NULL,
                                quantity_changed INT NOT NULL,
                                previous_quantity INT NOT NULL,
                                new_quantity INT NOT NULL,
                                reason NVARCHAR(255) NULL,
                                created_by BIGINT NULL,
                                created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
                                FOREIGN KEY (product_id) REFERENCES products(id),
                                FOREIGN KEY (created_by) REFERENCES users(id)
);
END
GO

-- 12. INITIAL SEED DATA
-- Default Roles
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN')
    INSERT INTO roles (name, description) VALUES ('ROLE_ADMIN', N'Quản trị viên hệ thống');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_STAFF')
    INSERT INTO roles (name, description) VALUES ('ROLE_STAFF', N'Nhân viên bán hàng/kho');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_CUSTOMER')
    INSERT INTO roles (name, description) VALUES ('ROLE_CUSTOMER', N'Khách hàng cá nhân');
GO

-- Default Admin Account (password: Admin@123 -> BCrypt hash)
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@fruitstore.vn')
BEGIN
    INSERT INTO users (full_name, email, password, phone_number, address, enabled, created_at)
    VALUES (N'Administrator', 'admin@fruitstore.vn', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901234567', N'Hà Nội, Việt Nam', 1, GETDATE());

    DECLARE @admin_id BIGINT = (SELECT id FROM users WHERE email = 'admin@fruitstore.vn');
    DECLARE @admin_role_id BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN');

    INSERT INTO user_roles (user_id, role_id) VALUES (@admin_id, @admin_role_id);
END
GO

-- Default Staff Account (password: Staff@123)
IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'staff@fruitstore.vn')
BEGIN
    INSERT INTO users (full_name, email, password, phone_number, address, enabled, created_at)
    VALUES (N'Nhân Viên 1', 'staff@fruitstore.vn', '$2a$12$4mU8g0HqX0E3.r3WvK0.v.XoXq7qZtB7z5K.6L7q2Y1lG8H9e7K9m', '0912345678', N'Hà Nội, Việt Nam', 1, GETDATE());

    DECLARE @staff_id BIGINT = (SELECT id FROM users WHERE email = 'staff@fruitstore.vn');
    DECLARE @staff_role_id BIGINT = (SELECT id FROM roles WHERE name = 'ROLE_STAFF');

    INSERT INTO user_roles (user_id, role_id) VALUES (@staff_id, @staff_role_id);
END
GO
