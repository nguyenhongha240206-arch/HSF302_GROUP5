
-- =====================================================
-- V7: CREATE ORDER_DETAIL_FRUITS
-- Purpose:
-- Store individual fruits inside a Custom Basket order
-- Database: Microsoft SQL Server
-- =====================================================

CREATE TABLE dbo.order_detail_fruits (
                                         id BIGINT IDENTITY(1,1) NOT NULL,

                                         order_detail_id BIGINT NOT NULL,
                                         product_id BIGINT NOT NULL,
                                         quantity INT NOT NULL,

                                         CONSTRAINT PK_order_detail_fruits
                                             PRIMARY KEY (id),

                                         CONSTRAINT FK_odf_order_details
                                             FOREIGN KEY (order_detail_id)
                                                 REFERENCES dbo.order_details(id),

                                         CONSTRAINT FK_odf_products
                                             FOREIGN KEY (product_id)
                                                 REFERENCES dbo.products(id),

                                         CONSTRAINT CK_odf_quantity
                                             CHECK (quantity > 0)
);

-- Index for loading fruits by order detail
CREATE INDEX IX_odf_order_detail_id
    ON dbo.order_detail_fruits(order_detail_id);

-- Index for looking up product components
CREATE INDEX IX_odf_product_id
    ON dbo.order_detail_fruits(product_id);
