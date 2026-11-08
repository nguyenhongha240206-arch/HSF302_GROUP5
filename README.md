# Fruit Store Management System 🍎🍇🍊
**Dự án môn học HSF302 - Lập trình ứng dụng Java Web với Spring Boot & Thymeleaf**
*Khoa Công Nghệ Thông Tin - Đại học FPT (Học kỳ 5 - Lớp SE2056-JV - Nhóm 5)*

---

## 📌 Giới Thiệu Dự Án
Hệ thống **Quản lý Cửa hàng Hoa quả Tươi & Giỏ Quà Tặng Cao Cấp (Fruit Store Management System)** được thiết kế và xây dựng theo mô hình kiến trúc MVC phân lớp chuẩn doanh nghiệp của môn học **HSF302**. Hệ thống cung cấp đầy đủ các trải nghiệm từ giao diện mua sắm khách hàng (Storefront) đến trang quản trị vận hành bán lẻ & kho hoa quả (Admin/Staff Portal).

### 👥 Nhóm Thực Hiện (Group 5 - SE2056-JV)
- Nguyễn Hồng Hà - HE200535
- Mai Thế Hữu - HE204591
- Triệu Xuân Bắc - HE194590
- Phạm Đức Hùng
- Phí Đình Minh
- Bùi Hữu Quý

---

## 🛠 Công Nghệ Sử Dụng
- **Java**: 17+
- **Framework**: Spring Boot 4.1.1 / 3.x
- **Bảo Mật**: Spring Security 6 (Form Login, RBAC, BCryptPasswordEncoder)
- **Dữ Liệu & ORM**: Spring Data JPA, Hibernate 6
- **Cơ Sở Dữ Liệu**: Microsoft SQL Server (kèm script SQL Server và Hibernate ddl-auto)
- **Template Engine**: Thymeleaf kết hợp `thymeleaf-layout-dialect` & `thymeleaf-extras-springsecurity6`
- **Giao Diện**: Bootstrap 5.3, Bootstrap Icons, Responsive Mobile/Desktop
- **Công Cụ Khác**: Lombok, Jakarta Bean Validation, Spring Boot DevTools

---

## 🌟 Các Tính Năng Nổi Bật & Nghiệp Vụ

### 1. Kiến Trúc Chuẩn HSF302 & Đề Tài Nhóm 5
- **JPA Auditing**: Kế thừa `BaseEntity`, tự động gán `created_at`, `updated_at`, `created_by`, `updated_by` từ `SecurityContextHolder`.
- **Soft Delete (Xóa mềm)**: Sử dụng các trường `is_deleted` và `deleted_at` cùng `@SQLRestriction("is_deleted = 0")`, không làm mất dữ liệu lịch sử hóa đơn khi xóa danh mục, hoa quả, voucher hay người dùng.
- **Tự động Seed dữ liệu mẫu**: Chạy ứng dụng lần đầu sẽ tự động nạp sẵn tài khoản Admin, Staff, Customer, danh mục, trái cây nhập khẩu/nội địa, giỏ quà VIP, mã voucher và đánh giá.

### 2. Phân Hệ Cửa Hàng (Storefront / Customer)
- **Trang chủ (`/`)**: Banner khuyến mãi, danh mục nổi bật, trái cây bán chạy, bộ sưu tập giỏ quà biếu sang trọng, mã giảm giá đang hoạt động.
- **Xem cửa hàng & Lọc đa tiêu chí (`/catalog`)**:
  - Tìm kiếm hoa quả theo từ khóa, xuất xứ.
  - Lọc theo danh mục, loại sản phẩm (Trái cây tươi, Giỏ quà, Hoa quả sấy).
  - Lọc theo khoảng giá (min - max).
  - Sắp xếp linh hoạt (Mới nhất, Giá tăng/giảm, Tên A-Z) kèm phân trang.
- **Chi tiết sản phẩm (`/products/{id}`)**: Thông tin xuất xứ, đơn vị tính, tình trạng kho, chọn số lượng, cam kết dịch vụ.
- **Đánh giá & Bình luận (`/reviews/add`)**: Khách hàng đã đăng nhập có thể đánh giá từ 1 - 5 sao và viết nhận xét.
- **Giỏ hàng (`/cart`)**: Lưu giỏ hàng theo Session, cập nhật số lượng, xóa mặt hàng, áp dụng hoặc hủy mã voucher giảm giá (kiểm tra điều kiện đơn hàng tối thiểu & hạn dùng).
- **Đặt hàng & Thanh toán (`/checkout`)**: Nhập thông tin người nhận, địa chỉ giao hàng, chọn thanh toán COD hoặc Chuyển khoản ngân hàng. Tự động kiểm tra và trừ tồn kho.
- **Lịch sử đơn hàng (`/orders/history`)**: Theo dõi lộ trình đơn hàng, xem chi tiết hóa đơn, được phép hủy đơn khi đơn còn ở trạng thái `Chờ xác nhận` (hệ thống sẽ tự động hoàn lại tồn kho).
- **Hồ sơ cá nhân (`/profile`)**: Cập nhật họ tên, số điện thoại, địa chỉ nhận hàng mặc định.

### 3. Phân Hệ Quản Trị (Admin & Staff Portal)
- **Bảng điều khiển (`/admin`)**:
  - Thống kê doanh thu đơn hàng đã giao thành công.
  - Số lượng đơn hàng chờ duyệt.
  - Cảnh báo các sản phẩm sắp hết hàng (tồn kho $\le$ 10).
  - Tổng giá trị thiệt hại do hao hụt/hủy hoa quả.
  - Danh sách đơn hàng mới nhất.
- **Quản lý Danh mục (`/admin/categories`)**: Thêm, sửa, xóa mềm, tìm kiếm, phân trang.
- **Quản lý Sản phẩm & Giỏ quà (`/admin/products`)**:
  - CRUD sản phẩm hoa quả, giỏ quà tặng, trái cây sấy.
  - Cập nhật số lượng tồn kho, giá bán, giá khuyến mãi.
  - Gắn nhãn sản phẩm nổi bật (Featured).
- **Quản lý Đơn hàng (`/admin/orders`)**:
  - Lọc đơn theo trạng thái: *Chờ xác nhận $\rightarrow$ Đã xác nhận $\rightarrow$ Đang giao hàng $\rightarrow$ Giao thành công / Đã hủy*.
  - Cập nhật trạng thái thanh toán (*Chưa thanh toán / Đã thanh toán*).
  - Khi hủy đơn hàng, hệ thống tự động hoàn trả số lượng hoa quả về kho.
- **Quản lý Kho & Phiếu Hao Hụt (`/admin/inventory/slips`)**:
  - *Nghiệp vụ đặc thù cho cửa hàng hoa quả tươi*: Lập phiếu xuất hủy hoa quả bị hư hỏng, dập nát, thối úng, quá hạn sử dụng, hao hụt lạnh.
  - Tự động trừ số lượng xuất hủy khỏi kho và tính tổng chi phí thiệt hại.
  - Hỗ trợ xem chi tiết phiếu hoặc hủy phiếu (hoàn trả lại kho).
- **Kiểm kê tồn kho (`/admin/inventory/stock`)**: Cảnh báo tức thì mặt hàng nào sắp hết để kịp thời nhập hàng.
- **Voucher Khuyến mãi (`/admin/vouchers`)**: Quản lý mã giảm giá, mức giảm %, giảm tối đa, đơn hàng tối thiểu, ngày bắt đầu/kết thúc, giới hạn lượt dùng.
- **Quản lý Người dùng (`/admin/users`)** *(Chỉ dành cho ADMIN)*: Xem danh sách tài khoản, khóa/mở khóa tài khoản, phân quyền.

---

## 🔑 Tài Khoản Đăng Nhập Mặc Định

| Vai Trò | Email Đăng Nhập | Mật Khẩu | Quyền Hạn |
|---|---|---|---|
| **Admin** | `admin@fruitstore.com` | `123456` | Toàn quyền quản trị hệ thống, duyệt đơn, kho, user |
| **Nhân viên (Staff)** | `staff@fruitstore.com` | `123456` | Quản lý sản phẩm, danh mục, đơn hàng, lập phiếu hao hụt |
| **Khách hàng** | `customer@fruitstore.com` | `123456` | Mua hàng, áp mã voucher, đánh giá, lịch sử đơn |

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### Bước 1: Khởi Tạo Cơ Sở Dữ Liệu SQL Server
1. Mở **SQL Server Management Studio (SSMS)** hoặc **Azure Data Studio**.
2. Mở và thực thi toàn bộ file `FruitStoreDB.sql` nằm ở thư mục gốc của dự án.
3. Script sẽ tự động tạo cơ sở dữ liệu `FruitStoreDB` cùng tất cả các bảng và ràng buộc khóa ngoại.

### Bước 2: Cấu Hình Kết Nối (Nếu cần)
Mở file `src/main/resources/application.properties` và kiểm tra tài khoản SQL Server của bạn (mặc định là `sa` / `sa`, port `1433`):
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=FruitStoreDB;encrypt=false;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=sa
```

### Bước 3: Biên Dịch & Khởi Chạy Ứng Dụng
Sử dụng Maven Wrapper có sẵn trong dự án:

**Trên Windows (PowerShell / Command Prompt):**
```powershell
./mvnw.cmd spring-boot:run
```

**Hoặc dùng Maven đã cài sẵn:**
```bash
mvn spring-boot:run
```

### Bước 4: Trải Nghiệm Ứng Dụng
- Trang chủ người dùng: **http://localhost:8080**
- Trang đăng nhập: **http://localhost:8080/auth/login**
- Trang quản trị: **http://localhost:8080/admin**

