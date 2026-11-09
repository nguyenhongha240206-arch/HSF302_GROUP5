-- V1 uses order_code/customer_name/customer_phone. Hibernate may already have
-- added order_number/receiver_name/receiver_phone to existing databases.
-- Keep both sets of data, but make the current entity columns authoritative.
IF COL_LENGTH('dbo.orders', 'order_number') IS NULL
    ALTER TABLE dbo.orders ADD order_number VARCHAR(50) NULL;
IF COL_LENGTH('dbo.orders', 'receiver_name') IS NULL
    ALTER TABLE dbo.orders ADD receiver_name VARCHAR(100) NULL;
IF COL_LENGTH('dbo.orders', 'receiver_phone') IS NULL
    ALTER TABLE dbo.orders ADD receiver_phone VARCHAR(20) NULL;
GO

IF COL_LENGTH('dbo.orders', 'order_code') IS NOT NULL
    EXEC sys.sp_executesql N'UPDATE dbo.orders SET order_number = order_code WHERE order_number IS NULL';
IF COL_LENGTH('dbo.orders', 'customer_name') IS NOT NULL
    EXEC sys.sp_executesql N'UPDATE dbo.orders SET receiver_name = customer_name WHERE receiver_name IS NULL';
IF COL_LENGTH('dbo.orders', 'customer_phone') IS NOT NULL
    EXEC sys.sp_executesql N'UPDATE dbo.orders SET receiver_phone = customer_phone WHERE receiver_phone IS NULL';
GO

-- A previous Hibernate update may have already created a unique index on the
-- new order_number column. Drop it during the type/nullability adjustment;
-- the unique index is recreated below.
DECLARE @order_number_uq SYSNAME;
DECLARE @ddl NVARCHAR(MAX);
SELECT TOP (1) @order_number_uq = kc.name
FROM sys.key_constraints kc
INNER JOIN sys.index_columns ic
    ON ic.object_id = kc.parent_object_id AND ic.index_id = kc.unique_index_id
INNER JOIN sys.columns c
    ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE kc.parent_object_id = OBJECT_ID('dbo.orders') AND kc.type = 'U' AND c.name = 'order_number';
IF @order_number_uq IS NOT NULL
BEGIN
    SET @ddl = N'ALTER TABLE dbo.orders DROP CONSTRAINT ' + QUOTENAME(@order_number_uq);
    EXEC sys.sp_executesql @ddl;
END;

DECLARE @order_number_index SYSNAME;
SELECT TOP (1) @order_number_index = i.name
FROM sys.indexes i
INNER JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id AND ic.key_ordinal = 1
INNER JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE i.object_id = OBJECT_ID('dbo.orders') AND i.is_unique = 1 AND c.name = 'order_number'
  AND NOT EXISTS (SELECT 1 FROM sys.key_constraints kc WHERE kc.parent_object_id = i.object_id AND kc.unique_index_id = i.index_id);
IF @order_number_index IS NOT NULL
BEGIN
    SET @ddl = N'DROP INDEX ' + QUOTENAME(@order_number_index) + N' ON dbo.orders';
    EXEC sys.sp_executesql @ddl;
END;

IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'order_number' AND is_nullable = 1)
    ALTER TABLE dbo.orders ALTER COLUMN order_number VARCHAR(50) NOT NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'receiver_name' AND is_nullable = 1)
    ALTER TABLE dbo.orders ALTER COLUMN receiver_name VARCHAR(100) NOT NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'receiver_phone' AND is_nullable = 1)
    ALTER TABLE dbo.orders ALTER COLUMN receiver_phone VARCHAR(20) NOT NULL;
GO

-- Remove the old unique constraint so old inserts can omit order_code.
DECLARE @legacy_code_constraint SYSNAME;
DECLARE @legacy_ddl NVARCHAR(MAX);
SELECT TOP (1) @legacy_code_constraint = kc.name
FROM sys.key_constraints kc
INNER JOIN sys.index_columns ic
    ON ic.object_id = kc.parent_object_id AND ic.index_id = kc.unique_index_id
INNER JOIN sys.columns c
    ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE kc.parent_object_id = OBJECT_ID('dbo.orders')
  AND kc.type = 'U'
  AND c.name = 'order_code';

IF @legacy_code_constraint IS NOT NULL
BEGIN
    SET @legacy_ddl = N'ALTER TABLE dbo.orders DROP CONSTRAINT ' + QUOTENAME(@legacy_code_constraint);
    EXEC sys.sp_executesql @legacy_ddl;
END;
GO

-- Legacy columns stay in place for existing data, but new inserts use the
-- current entity columns. Preserve uniqueness for any historical order codes.
IF COL_LENGTH('dbo.orders', 'order_code') IS NOT NULL
BEGIN
    ALTER TABLE dbo.orders ALTER COLUMN order_code NVARCHAR(50) NULL;

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'UX_orders_legacy_order_code')
        CREATE UNIQUE INDEX UX_orders_legacy_order_code
            ON dbo.orders(order_code) WHERE order_code IS NOT NULL;
END;

IF COL_LENGTH('dbo.orders', 'customer_name') IS NOT NULL
    ALTER TABLE dbo.orders ALTER COLUMN customer_name NVARCHAR(100) NULL;
IF COL_LENGTH('dbo.orders', 'customer_phone') IS NOT NULL
    ALTER TABLE dbo.orders ALTER COLUMN customer_phone NVARCHAR(20) NULL;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes i
    INNER JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id AND ic.key_ordinal = 1
    INNER JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
    WHERE i.object_id = OBJECT_ID('dbo.orders') AND i.is_unique = 1 AND c.name = 'order_number'
)
    CREATE UNIQUE INDEX UX_orders_order_number ON dbo.orders(order_number);
GO
