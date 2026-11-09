-- =====================================================================
-- DATABASE MIGRATION SCRIPT: V5__convert_all_text_columns_to_nvarchar_and_fix_vietnamese.sql
-- PURPOSE: Convert all text columns from VARCHAR to NVARCHAR for Unicode UTF-16 support
--          and restore correct Vietnamese accented data.
-- =====================================================================

-- 1. ALTER USERS COLUMNS TO NVARCHAR
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.users') AND name = 'full_name' AND system_type_id = 167)
    ALTER TABLE dbo.users ALTER COLUMN full_name NVARCHAR(100) NOT NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.users') AND name = 'address' AND system_type_id = 167)
    ALTER TABLE dbo.users ALTER COLUMN address NVARCHAR(255) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.users') AND name = 'phone_number' AND system_type_id = 167)
    ALTER TABLE dbo.users ALTER COLUMN phone_number NVARCHAR(20) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.users') AND name = 'created_by' AND system_type_id = 167)
    ALTER TABLE dbo.users ALTER COLUMN created_by NVARCHAR(100) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.users') AND name = 'updated_by' AND system_type_id = 167)
    ALTER TABLE dbo.users ALTER COLUMN updated_by NVARCHAR(100) NULL;
GO

-- 2. ALTER ROLES COLUMNS TO NVARCHAR
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.roles') AND name = 'description' AND system_type_id = 167)
    ALTER TABLE dbo.roles ALTER COLUMN description NVARCHAR(255) NULL;
GO

-- 3. ALTER CATEGORIES COLUMNS TO NVARCHAR
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.categories') AND name = 'name' AND system_type_id = 167)
    ALTER TABLE dbo.categories ALTER COLUMN name NVARCHAR(100) NOT NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.categories') AND name = 'description' AND system_type_id = 167)
    ALTER TABLE dbo.categories ALTER COLUMN description NVARCHAR(500) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.categories') AND name = 'created_by' AND system_type_id = 167)
    ALTER TABLE dbo.categories ALTER COLUMN created_by NVARCHAR(100) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.categories') AND name = 'updated_by' AND system_type_id = 167)
    ALTER TABLE dbo.categories ALTER COLUMN updated_by NVARCHAR(100) NULL;
GO

-- 4. ALTER PRODUCTS COLUMNS TO NVARCHAR
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.products') AND name = 'name' AND system_type_id = 167)
    ALTER TABLE dbo.products ALTER COLUMN name NVARCHAR(150) NOT NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.products') AND name = 'origin' AND system_type_id = 167)
    ALTER TABLE dbo.products ALTER COLUMN origin NVARCHAR(100) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.products') AND name = 'unit' AND system_type_id = 167)
    ALTER TABLE dbo.products ALTER COLUMN unit NVARCHAR(50) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.products') AND name = 'description' AND system_type_id = 167)
    ALTER TABLE dbo.products ALTER COLUMN description NVARCHAR(MAX) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.products') AND name = 'created_by' AND system_type_id = 167)
    ALTER TABLE dbo.products ALTER COLUMN created_by NVARCHAR(100) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.products') AND name = 'updated_by' AND system_type_id = 167)
    ALTER TABLE dbo.products ALTER COLUMN updated_by NVARCHAR(100) NULL;
GO

-- 5. ALTER ORDERS COLUMNS TO NVARCHAR
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'receiver_name' AND system_type_id = 167)
    ALTER TABLE dbo.orders ALTER COLUMN receiver_name NVARCHAR(100) NOT NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'receiver_phone' AND system_type_id = 167)
    ALTER TABLE dbo.orders ALTER COLUMN receiver_phone NVARCHAR(20) NOT NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'shipping_address' AND system_type_id = 167)
    ALTER TABLE dbo.orders ALTER COLUMN shipping_address NVARCHAR(300) NOT NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'note' AND system_type_id = 167)
    ALTER TABLE dbo.orders ALTER COLUMN note NVARCHAR(500) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'packaging_requirement' AND system_type_id = 167)
    ALTER TABLE dbo.orders ALTER COLUMN packaging_requirement NVARCHAR(200) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'delivery_time_slot' AND system_type_id = 167)
    ALTER TABLE dbo.orders ALTER COLUMN delivery_time_slot NVARCHAR(100) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'delivery_time_type' AND system_type_id = 167)
    ALTER TABLE dbo.orders ALTER COLUMN delivery_time_type NVARCHAR(50) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'created_by' AND system_type_id = 167)
    ALTER TABLE dbo.orders ALTER COLUMN created_by NVARCHAR(100) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.orders') AND name = 'updated_by' AND system_type_id = 167)
    ALTER TABLE dbo.orders ALTER COLUMN updated_by NVARCHAR(100) NULL;
GO

-- 6. ALTER ORDER_DETAILS COLUMNS TO NVARCHAR
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.order_details') AND name = 'product_name' AND system_type_id = 167)
    ALTER TABLE dbo.order_details ALTER COLUMN product_name NVARCHAR(150) NOT NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.order_details') AND name = 'item_details' AND system_type_id = 167)
    ALTER TABLE dbo.order_details ALTER COLUMN item_details NVARCHAR(1000) NULL;
GO

-- 7. ALTER VOUCHERS COLUMNS TO NVARCHAR
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.vouchers') AND name = 'name' AND system_type_id = 167)
    ALTER TABLE dbo.vouchers ALTER COLUMN name NVARCHAR(150) NOT NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.vouchers') AND name = 'created_by' AND system_type_id = 167)
    ALTER TABLE dbo.vouchers ALTER COLUMN created_by NVARCHAR(100) NULL;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.vouchers') AND name = 'updated_by' AND system_type_id = 167)
    ALTER TABLE dbo.vouchers ALTER COLUMN updated_by NVARCHAR(100) NULL;
GO

-- 8. ALTER REVIEWS COLUMNS TO NVARCHAR (if exists)
IF OBJECT_ID('dbo.reviews') IS NOT NULL
BEGIN
    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.reviews') AND name = 'comment' AND system_type_id = 167)
        ALTER TABLE dbo.reviews ALTER COLUMN comment NVARCHAR(1000) NULL;
END
GO

-- 9. ALTER LOSS_SLIPS & LOSS_SLIP_DETAILS (if exists)
IF OBJECT_ID('dbo.loss_slips') IS NOT NULL
BEGIN
    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.loss_slips') AND name = 'note' AND system_type_id = 167)
        ALTER TABLE dbo.loss_slips ALTER COLUMN note NVARCHAR(500) NULL;
END
GO
IF OBJECT_ID('dbo.loss_slip_details') IS NOT NULL
BEGIN
    IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.loss_slip_details') AND name = 'note' AND system_type_id = 167)
        ALTER TABLE dbo.loss_slip_details ALTER COLUMN note NVARCHAR(255) NULL;
END
GO

-- =====================================================================
-- 10. RESTORE AUTHENTIC VIETNAMESE DATA WITH UNICODE (N'...')
-- =====================================================================

-- Roles
UPDATE dbo.roles SET description = N'Quản trị viên toàn quyền' WHERE name = 'ROLE_ADMIN';
UPDATE dbo.roles SET description = N'Nhân viên quản lý cửa hàng' WHERE name = 'ROLE_STAFF';
UPDATE dbo.roles SET description = N'Khách hàng' WHERE name = 'ROLE_CUSTOMER';
GO

-- Users
UPDATE dbo.users SET full_name = N'Nguyễn Hồng Hà', address = N'Cầu Giấy, Hà Nội' WHERE email = 'customer@fruitstore.com';
UPDATE dbo.users SET full_name = N'Nguyễn Quản Trị', address = N'FPT University, Khu CNC Hòa Lạc, Hà Nội' WHERE email = 'admin@fruitstore.com';
UPDATE dbo.users SET full_name = N'Trần Nhân Viên', address = N'Thạch Thất, Hà Nội' WHERE email = 'staff@fruitstore.com';
UPDATE dbo.users SET full_name = N'Quản Trị Viên Demo', address = N'Hà Nội' WHERE email = 'admin.demo@fruitstore.com';
UPDATE dbo.users SET full_name = N'Nhân Viên Demo', address = N'Hà Nội' WHERE email = 'staff.demo@fruitstore.com';
UPDATE dbo.users SET full_name = N'Khách Hàng Demo', address = N'Hà Nội' WHERE email = 'customer.demo@fruitstore.com';
GO

-- Categories
UPDATE dbo.categories SET name = N'Trái cây nhập khẩu', description = N'Các loại trái cây cao cấp nhập khẩu đường hàng không từ Mỹ, Úc, New Zealand, Nhật, Hàn' WHERE code = 'CAT_IMPORT';
UPDATE dbo.categories SET name = N'Trái cây đặc sản Việt', description = N'Hoa quả đặc sản ba miền Việt Nam đạt chuẩn VietGAP, GlobalGAP tươi ngon' WHERE code = 'CAT_LOCAL';
UPDATE dbo.categories SET name = N'Giỏ quà trái cây cao cấp', description = N'Giỏ quà hoa quả trang trọng kèm nơ lụa và hoa tươi thích hợp biếu tặng, chúc mừng, dạm ngõ' WHERE code = 'CAT_GIFT';
UPDATE dbo.categories SET name = N'Trái cây sấy khô & Chế biến', description = N'Hoa quả sấy dẻo, sấy thăng hoa giữ nguyên hương vị và dinh dưỡng tự nhiên' WHERE code = 'CAT_DRIED';
GO

-- Vouchers
UPDATE dbo.vouchers SET name = N'Chào mừng bạn mới - Giảm ngay 10%' WHERE code = 'CHAOBANMOI';
UPDATE dbo.vouchers SET name = N'Ưu đãi giảm giá 15% cho đơn từ 500k' WHERE code = 'FREESHIP';
UPDATE dbo.vouchers SET name = N'Tri ân khách hàng thân thiết - Giảm 20%' WHERE code = 'TRIAN50K';
GO

-- Products
UPDATE dbo.products SET name = N'Táo Envy New Zealand Size 70', origin = N'New Zealand', unit = N'kg', description = N'Táo Envy New Zealand nhập khẩu chính ngạch, thịt giòn tan, vị ngọt đậm đà và hương thơm ngát tự nhiên.' WHERE code = 'FRUIT_ENVY_NZ';
UPDATE dbo.products SET name = N'Nho mẫu đơn Shine Muscat Nhật', origin = N'Nhật Bản', unit = N'hộp', description = N'Nho mẫu đơn Shine Muscat chuẩn hàng bay xách tay, quả to bóng mịn, không hạt, thơm hương sữa ngào ngạt.' WHERE code = 'FRUIT_MUSCAT_JP';
UPDATE dbo.products SET name = N'Nho ngón tay Midnight Beauty Úc', origin = N'Úc', unit = N'kg', description = N'Nho đen không hạt ngón tay Úc hình dáng thon dài độc đáo, thịt quả giòn rụm, vị ngọt thanh đậm đà.' WHERE code = 'FRUIT_FINGER_AU';
UPDATE dbo.products SET name = N'Cam Cara Cara ruột đỏ Úc', origin = N'Úc', unit = N'kg', description = N'Cam không hạt ruột đỏ Cara ruột đỏ mọng nước, hàm lượng vitamin C cực cao, ngọt thanh mát.' WHERE code = 'FRUIT_CARA_AU';
UPDATE dbo.products SET name = N'Cam vàng Navel Úc không hạt', origin = N'Úc', unit = N'kg', description = N'Cam vàng Navel vỏ mỏng dễ bóc, múi cam vàng mọng nước, thơm lừng vị ngọt tươi mát.' WHERE code = 'FRUIT_NAVEL_AU';
UPDATE dbo.products SET name = N'Cherry đỏ Mỹ Size 8.5 chuẩn bay', origin = N'Mỹ', unit = N'hộp', description = N'Cherry Washington đỏ thẫm cuống xanh tươi rói, quả chắc nịch, thịt ngọt đậm giòn tan đẳng cấp trái cây thượng hạng.' WHERE code = 'FRUIT_CHERRY_US';
UPDATE dbo.products SET name = N'Dâu tây Hàn Quốc Seolhyang cao cấp', origin = N'Hàn Quốc', unit = N'hộp', description = N'Dâu giống Seolhyang nổi tiếng xứ kim chi, quả đỏ mọng hình nón hoàn hảo, vị ngọt ngào và mùi thơm quyến rũ đặc trưng.' WHERE code = 'FRUIT_STRAW_KR';
UPDATE dbo.products SET name = N'Kiwi vàng Zespri SunGold New Zealand', origin = N'New Zealand', unit = N'kg', description = N'Kiwi vàng Zespri ruột vàng ươm mọng nước, giàu vitamin C gấp 3 lần cam, vị chua ngọt cân bằng tinh tế.' WHERE code = 'FRUIT_KIWI_NZ';
UPDATE dbo.products SET name = N'Lê nâu Hàn Quốc Singold quả to giòn', origin = N'Hàn Quốc', unit = N'kg', description = N'Lê nâu Hàn Quốc chuẩn xuất khẩu quả to tròn đều, vỏ nâu mỏng tang, thịt trắng giòn nhiều nước ngọt thanh tao.' WHERE code = 'FRUIT_PEAR_KR';
UPDATE dbo.products SET name = N'Việt quất tươi hữu cơ Driscoll''s Mỹ', origin = N'Mỹ', unit = N'hộp', description = N'Việt quất quả chắc mẩy phủ phấn trắng tự nhiên, giàu chất chống oxy hóa anthocyanin tốt cho sức khỏe và sắc đẹp.' WHERE code = 'FRUIT_BLUEBERRY_US';
UPDATE dbo.products SET name = N'Xoài Cát Hòa Lộc Tiền Giang loại 1', origin = N'Việt Nam', unit = N'kg', description = N'Xoài cát Hòa Lộc chính gốc Tiền Giang, thịt dày mịn, không xơ, hương thơm nồng nàn vị ngọt lịm.' WHERE code = 'FRUIT_XOAI_HL';
UPDATE dbo.products SET name = N'Bưởi da xanh Bến Tre ruột hồng', origin = N'Việt Nam', unit = N'quả', description = N'Bưởi da xanh loại 1 cành lá sum suê, tép bưởi mọng nước, giòn ráo không hạt vị chua ngọt cân bằng.' WHERE code = 'FRUIT_BUOI_BT';
UPDATE dbo.products SET name = N'Sầu riêng Ri6 cơm vàng hạt lép Đắk Lắk', origin = N'Việt Nam', unit = N'kg', description = N'Sầu riêng Ri6 chín cây tự nhiên, cơm vàng óng ả dày cùi béo ngậy hạt lép, hương thơm quyến rũ nức mũi.' WHERE code = 'FRUIT_SAURIENG_RI6';
UPDATE dbo.products SET name = N'Dưa lưới Huỳnh Long ruột cam ngọt mát', origin = N'Việt Nam', unit = N'kg', description = N'Dưa lưới trồng nhà màng công nghệ cao, vân lưới dày đều đẹp, ruột cam giòn ngọt lịm hương thơm mát.' WHERE code = 'FRUIT_DUALUOI_HL';
UPDATE dbo.products SET name = N'Măng cụt Lái Thiêu chuẩn VietGAP', origin = N'Việt Nam', unit = N'kg', description = N'Nữ hoàng trái cây nhiệt đới Lái Thiêu vỏ mềm mỏng, múi trắng nõn mọng nước, vị chua ngọt thanh tao.' WHERE code = 'FRUIT_MANGCUT_LT';
UPDATE dbo.products SET name = N'Thanh long ruột đỏ Bình Thuận xuất khẩu', origin = N'Việt Nam', unit = N'kg', description = N'Thanh long ruột đỏ đậm tự nhiên, ngọt đậm thơm ngon, giàu chất xơ và chất chống oxy hóa tự nhiên.' WHERE code = 'FRUIT_THANHLONG_BT';
UPDATE dbo.products SET name = N'Giỏ Trái Cây Ngũ Phúc Đoàn Viên', origin = N'Việt Nam / Nhập khẩu', unit = N'giỏ', description = N'Thiết kế giỏ mây tre đan cao cấp, kết hợp Táo Envy, Nho mẫu đơn, Cam Cara, Lê Hàn Quốc và Kiwi vàng. Kèm hoa lá tươi, ruy băng sang trọng.' WHERE code = 'GIFT_NGUPHUC_01';
UPDATE dbo.products SET name = N'Hộp Quà Trái Cây Doanh Nghiệp VIP', origin = N'Nhập khẩu', unit = N'hộp', description = N'Hộp quà nắp kính ép nhũ sang trọng, tuyển chọn Cherry Mỹ, Dâu tây Hàn Quốc, Nho xanh không hạt. Đẳng cấp quà biếu đối tác.' WHERE code = 'GIFT_VIP_BIZ';
UPDATE dbo.products SET name = N'Giỏ Quà Hoa Quả Dạm Ngõ Thắm Duyên', origin = N'Nhập khẩu & Đặc sản', unit = N'giỏ', description = N'Mẫu giỏ quà cưới hỏi, dạm ngõ thiết kế thắt nơ lụa đỏ song hỷ, kết hợp nho đen không hạt, táo đỏ Ruby, cam vàng và dưa lưới.' WHERE code = 'GIFT_DAMNGO_02';
UPDATE dbo.products SET name = N'Giỏ Quà Trái Cây Thắp Hương Lễ Phật', origin = N'Nhập khẩu & Việt Nam', unit = N'giỏ', description = N'Tuyển chọn các quả tươi cứng cáp đều đẹp: Bưởi da xanh cành lá, Táo đỏ, Cam vàng, Xoài cát dâng hương ban thờ, tổ tiên, lễ chùa.' WHERE code = 'GIFT_LEPHAT_03';
UPDATE dbo.products SET name = N'Xoài sấy dẻo xuất khẩu loại 1', origin = N'Việt Nam', unit = N'gói', description = N'Xoài cát sấy dẻo công nghệ lạnh, không chất bảo quản, giữ nguyên vị chua ngọt tự nhiên dẻo thơm.' WHERE code = 'DRIED_XOAI_DEO';
UPDATE dbo.products SET name = N'Mít sấy giòn nguyên miếng Dalat', origin = N'Việt Nam', unit = N'gói', description = N'Mít nghệ tươi sấy chân không giòn rụm, màu vàng óng tự nhiên, không đường hóa học, thơm lừng vị mít chín.' WHERE code = 'DRIED_MIT_DALAT';
UPDATE dbo.products SET name = N'Dâu tây sấy thăng hoa giữ trọn vitamin', origin = N'Việt Nam', unit = N'hộp', description = N'Dâu tây Đà Lạt sấy thăng hoa công nghệ Nhật Bản, giữ nguyên hình dáng quả, vị chua ngọt giòn xốp tan trong miệng.' WHERE code = 'DRIED_STRAW_FREEZE';
UPDATE dbo.products SET name = N'Hạt macca sấy nứt vỏ Đắk Lắk VIP', origin = N'Việt Nam', unit = N'hộp', description = N'Hạt macca loại 1 nứt vỏ tự nhiên kèm dụng cụ tách, nhân trắng ngà béo ngậy ngọt bùi dinh dưỡng cho cả gia đình.' WHERE code = 'DRIED_MACCA_DL';
GO

-- Reviews
IF OBJECT_ID('dbo.reviews') IS NOT NULL
BEGIN
    UPDATE dbo.reviews SET comment = N'Táo rất tươi và giòn ngọt, vỏ mỏng bóng đẹp! Giao hàng nhanh và đóng gói bọc xốp rất cẩn thận.'
    WHERE comment LIKE '%Táo%' OR comment LIKE '%giòn%';
END
GO
