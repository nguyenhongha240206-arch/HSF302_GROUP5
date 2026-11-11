-- V7: Tạo bảng Banners và User Vouchers (Module 3 - HaNH UC-14, UC-15)

-- 1. Bảng Banners (Quản lý banner khuyến mãi theo mùa - UC-14)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'banners')
BEGIN
    CREATE TABLE banners (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        title NVARCHAR(255) NOT NULL,
        subtitle NVARCHAR(500),
        image_url NVARCHAR(1000) NOT NULL,
        target_url NVARCHAR(500) DEFAULT '/catalog',
        position NVARCHAR(50) DEFAULT 'HERO_SLIDER', -- HERO_SLIDER, PROMO_BANNER, SIDEBAR
        display_order INT DEFAULT 0,
        start_date DATE NOT NULL,
        end_date DATE NOT NULL,
        active BIT DEFAULT 1 NOT NULL,
        is_deleted BIT DEFAULT 0 NOT NULL,
        created_at DATETIME2 DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2,
        created_by NVARCHAR(100),
        updated_by NVARCHAR(100)
    );
END;

-- 2. Bảng User Vouchers (Ví Voucher cá nhân của khách hàng - UC-15)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'user_vouchers')
BEGIN
    CREATE TABLE user_vouchers (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        voucher_id BIGINT NOT NULL,
        collected_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        is_used BIT DEFAULT 0 NOT NULL,
        used_at DATETIME2,
        is_deleted BIT DEFAULT 0 NOT NULL,
        created_at DATETIME2 DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2,
        created_by NVARCHAR(100),
        updated_by NVARCHAR(100),
        CONSTRAINT fk_user_vouchers_user FOREIGN KEY (user_id) REFERENCES users(id),
        CONSTRAINT fk_user_vouchers_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
        CONSTRAINT uq_user_voucher UNIQUE (user_id, voucher_id)
    );
END;

-- 3. Seed dữ liệu Banner mẫu ban đầu
IF NOT EXISTS (SELECT 1 FROM banners WHERE title LIKE N'%Lễ Hội Hoa Quả%')
BEGIN
    INSERT INTO banners (title, subtitle, image_url, target_url, position, display_order, start_date, end_date, active, is_deleted, created_at)
    VALUES 
    (N'Lễ Hội Hoa Quả Mùa Hè - Tươi Ngon Giòn Ngọt', 
     N'Chiết khấu lên tới 25% cho tất cả các dòng Cherry Mỹ, Nho mẫu đơn & Táo Envy New Zealand đạt chuẩn GlobalGAP.', 
     N'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=1200&q=80', 
     N'/catalog', 'HERO_SLIDER', 1, DATEADD(day, -5, CAST(GETDATE() AS DATE)), DATEADD(day, 60, CAST(GETDATE() AS DATE)), 1, 0, CURRENT_TIMESTAMP),

    (N'Thiết Kế Giỏ Quà Hoa Quả Độc Quyền Theo Yêu Cầu', 
     N'Tự tay chọn mẫu giỏ mây tre đan, phối các loại trái cây thượng hạng, tặng kèm nơ lụa & thiệp mừng cá nhân hóa.', 
     N'https://images.unsplash.com/photo-1546548970-71785318a17b?auto=format&fit=crop&w=1200&q=80', 
     N'/custom-basket', 'HERO_SLIDER', 2, DATEADD(day, -5, CAST(GETDATE() AS DATE)), DATEADD(day, 90, CAST(GETDATE() AS DATE)), 1, 0, CURRENT_TIMESTAMP),

    (N'Đặc Sản Trái Cây Ba Miền Chuẩn VietGAP', 
     N'Sầu riêng Ri6 chín cây, Bơ sáp 034 Đắk Lắk, Xoài cát Hòa Lộc thu hái trực tiếp từ nhà vườn uy tín.', 
     N'https://images.unsplash.com/photo-1587049352846-4a222e784d38?auto=format&fit=crop&w=1200&q=80', 
     N'/catalog?categoryId=2', 'HERO_SLIDER', 3, DATEADD(day, -5, CAST(GETDATE() AS DATE)), DATEADD(day, 45, CAST(GETDATE() AS DATE)), 1, 0, CURRENT_TIMESTAMP);
END;

