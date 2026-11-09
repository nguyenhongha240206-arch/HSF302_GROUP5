IF EXISTS (
    SELECT 1
    FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.order_details')
      AND name = N'product_id'
      AND is_nullable = 0
)
BEGIN
    ALTER TABLE dbo.order_details ALTER COLUMN product_id BIGINT NULL;
END
