package fu.fruitstore.shared.config;

import fu.fruitstore.auth.entity.Role;
import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.repository.RoleRepository;
import fu.fruitstore.auth.repository.UserRepository;
import fu.fruitstore.category.entity.Category;
import fu.fruitstore.category.repository.CategoryRepository;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.entity.ProductType;
import fu.fruitstore.product.repository.ProductRepository;
import fu.fruitstore.review.entity.Review;
import fu.fruitstore.review.repository.ReviewRepository;
import fu.fruitstore.voucher.entity.Voucher;
import fu.fruitstore.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            log.info(">>> Khởi tạo dữ liệu mẫu (Seeding Database)...");

            // 1. Roles
            Role roleAdmin = roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Quản trị viên toàn quyền").build());
            Role roleStaff = roleRepository.save(Role.builder().name("ROLE_STAFF").description("Nhân viên quản lý cửa hàng").build());
            Role roleCustomer = roleRepository.save(Role.builder().name("ROLE_CUSTOMER").description("Khách hàng").build());

            // 2. Users
            User admin = User.builder()
                    .fullName("Nguyễn Quản Trị")
                    .email("admin@fruitstore.com")
                    .password(passwordEncoder.encode("123456"))
                    .phoneNumber("0988111222")
                    .address("FPT University, Khu CNC Hòa Lạc, Hà Nội")
                    .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80")
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(roleAdmin, roleStaff)))
                    .build();
            userRepository.save(admin);

            User staff = User.builder()
                    .fullName("Trần Nhân Viên")
                    .email("staff@fruitstore.com")
                    .password(passwordEncoder.encode("123456"))
                    .phoneNumber("0977222333")
                    .address("Thạch Thất, Hà Nội")
                    .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80")
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(roleStaff)))
                    .build();
            userRepository.save(staff);

            User customer = User.builder()
                    .fullName("Nguyễn Hồng Hà")
                    .email("customer@fruitstore.com")
                    .password(passwordEncoder.encode("123456"))
                    .phoneNumber("0912345678")
                    .address("Cầu Giấy, Hà Nội")
                    .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80")
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(roleCustomer)))
                    .build();
            userRepository.save(customer);

            // 3. Categories
            Category catImport = categoryRepository.save(Category.builder()
                    .name("Trái cây nhập khẩu")
                    .code("CAT_IMPORT")
                    .description("Các loại trái cây cao cấp nhập khẩu đường hàng không từ Mỹ, Úc, New Zealand, Nhật, Hàn")
                    .imageUrl("https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=600&q=80")
                    .displayOrder(1)
                    .active(true)
                    .build());

            Category catLocal = categoryRepository.save(Category.builder()
                    .name("Trái cây đặc sản Việt")
                    .code("CAT_LOCAL")
                    .description("Hoa quả đặc sản ba miền Việt Nam đạt chuẩn VietGAP, GlobalGAP tươi ngon")
                    .imageUrl("https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=600&q=80")
                    .displayOrder(2)
                    .active(true)
                    .build());

            Category catGift = categoryRepository.save(Category.builder()
                    .name("Giỏ quà trái cây cao cấp")
                    .code("CAT_GIFT")
                    .description("Giỏ quà hoa quả trang trọng kèm nơ lụa và hoa tươi thích hợp biếu tặng, chúc mừng, dạm ngõ")
                    .imageUrl("https://images.unsplash.com/photo-1543083477-4f785aeafaa9?auto=format&fit=crop&w=600&q=80")
                    .displayOrder(3)
                    .active(true)
                    .build());

            Category catDried = categoryRepository.save(Category.builder()
                    .name("Trái cây sấy khô & Chế biến")
                    .code("CAT_DRIED")
                    .description("Hoa quả sấy dẻo, sấy thăng hoa giữ nguyên hương vị và dinh dưỡng tự nhiên")
                    .imageUrl("https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&w=600&q=80")
                    .displayOrder(4)
                    .active(true)
                    .build());

            // 4. Products
            Product p1 = productRepository.save(Product.builder()
                    .name("Táo Envy New Zealand Size 70")
                    .code("FRUIT_ENVY_NZ")
                    .description("Táo Envy New Zealand nhập khẩu chính ngạch, thịt giòn tan, vị ngọt đậm đà và hương thơm ngát tự nhiên.")
                    .price(new BigDecimal("220000"))
                    .originalPrice(new BigDecimal("250000"))
                    .stockQuantity(85)
                    .unit("kg")
                    .origin("New Zealand")
                    .imageUrl("https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80")
                    .productType(ProductType.FRUIT)
                    .category(catImport)
                    .featured(true)
                    .active(true)
                    .build());

            Product p2 = productRepository.save(Product.builder()
                    .name("Nho mẫu đơn Shine Muscat Nhật")
                    .code("FRUIT_MUSCAT_JP")
                    .description("Nho mẫu đơn Shine Muscat chuẩn hàng bay xách tay, quả to bóng mịn, không hạt, thơm hương sữa ngào ngạt.")
                    .price(new BigDecimal("850000"))
                    .originalPrice(new BigDecimal("950000"))
                    .stockQuantity(30)
                    .unit("hộp")
                    .origin("Nhật Bản")
                    .imageUrl("https://images.unsplash.com/photo-1596363505729-4190a9506133?auto=format&fit=crop&w=600&q=80")
                    .productType(ProductType.FRUIT)
                    .category(catImport)
                    .featured(true)
                    .active(true)
                    .build());

            Product p3 = productRepository.save(Product.builder()
                    .name("Cam Cara Cara ruột đỏ Úc")
                    .code("FRUIT_CARA_AU")
                    .description("Cam không hạt ruột đỏ Cara ruột đỏ mọng nước, hàm lượng vitamin C cực cao, ngọt thanh mát.")
                    .price(new BigDecimal("135000"))
                    .originalPrice(new BigDecimal("160000"))
                    .stockQuantity(120)
                    .unit("kg")
                    .origin("Úc")
                    .imageUrl("https://images.unsplash.com/photo-1582979512210-99b6a53386f9?auto=format&fit=crop&w=600&q=80")
                    .productType(ProductType.FRUIT)
                    .category(catImport)
                    .featured(false)
                    .active(true)
                    .build());

            Product p4 = productRepository.save(Product.builder()
                    .name("Xoài Cát Hòa Lộc Tiền Giang")
                    .code("FRUIT_XOAI_HL")
                    .description("Xoài cát Hòa Lộc chính gốc Tiền Giang, thịt dày mịn, không xơ, hương thơm nồng nàn vị ngọt lịm.")
                    .price(new BigDecimal("110000"))
                    .originalPrice(new BigDecimal("130000"))
                    .stockQuantity(65)
                    .unit("kg")
                    .origin("Việt Nam")
                    .imageUrl("https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=600&q=80")
                    .productType(ProductType.FRUIT)
                    .category(catLocal)
                    .featured(true)
                    .active(true)
                    .build());

            Product p5 = productRepository.save(Product.builder()
                    .name("Bưởi da xanh Bến Tre ruột hồng")
                    .code("FRUIT_BUOI_BT")
                    .description("Bưởi da xanh loại 1 cành lá sum suê, tép bưởi mọng nước, giòn ráo không hạt vị chua ngọt cân bằng.")
                    .price(new BigDecimal("85000"))
                    .originalPrice(new BigDecimal("95000"))
                    .stockQuantity(50)
                    .unit("quả")
                    .origin("Việt Nam")
                    .imageUrl("https://images.unsplash.com/photo-1577234286642-fc512a5f8f11?auto=format&fit=crop&w=600&q=80")
                    .productType(ProductType.FRUIT)
                    .category(catLocal)
                    .featured(false)
                    .active(true)
                    .build());

            Product p6 = productRepository.save(Product.builder()
                    .name("Giỏ Trái Cây Ngũ Phúc Đoàn Viên")
                    .code("GIFT_NGUPHUC_01")
                    .description("Thiết kế giỏ mây tre đan cao cấp, kết hợp Táo Envy, Nho mẫu đơn, Cam Cara, Lê Hàn Quốc và Kiwi vàng. Kèm hoa lá tươi, ruy băng sang trọng.")
                    .price(new BigDecimal("1250000"))
                    .originalPrice(new BigDecimal("1500000"))
                    .stockQuantity(15)
                    .unit("giỏ")
                    .origin("Việt Nam / Nhập khẩu")
                    .imageUrl("https://images.unsplash.com/photo-1543083477-4f785aeafaa9?auto=format&fit=crop&w=600&q=80")
                    .productType(ProductType.GIFT_BASKET)
                    .category(catGift)
                    .featured(true)
                    .active(true)
                    .build());

            Product p7 = productRepository.save(Product.builder()
                    .name("Hộp Quà Trái Cây Doanh Nghiệp VIP")
                    .code("GIFT_VIP_BIZ")
                    .description("Hộp quà nắp kính ép nhũ sang trọng, tuyển chọn Cherry Mỹ, Dâu tây Hàn Quốc, Nho xanh không hạt. Đẳng cấp quà biếu đối tác.")
                    .price(new BigDecimal("1850000"))
                    .originalPrice(new BigDecimal("2100000"))
                    .stockQuantity(8) // Low stock example (< 10)
                    .unit("hộp")
                    .origin("Nhập khẩu")
                    .imageUrl("https://images.unsplash.com/photo-1528825871115-3581a5387919?auto=format&fit=crop&w=600&q=80")
                    .productType(ProductType.GIFT_BASKET)
                    .category(catGift)
                    .featured(true)
                    .active(true)
                    .build());

            Product p8 = productRepository.save(Product.builder()
                    .name("Xoài sấy dẻo xuất khẩu loại 1")
                    .code("DRIED_XOAI_DEO")
                    .description("Xoài cát sấy dẻo công nghệ lạnh, không chất bảo quản, giữ nguyên vị chua ngọt tự nhiên dẻo thơm.")
                    .price(new BigDecimal("75000"))
                    .originalPrice(new BigDecimal("90000"))
                    .stockQuantity(150)
                    .unit("gói")
                    .origin("Việt Nam")
                    .imageUrl("https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&w=600&q=80")
                    .productType(ProductType.DRIED_FRUIT)
                    .category(catDried)
                    .featured(false)
                    .active(true)
                    .build());

            // 5. Vouchers
            voucherRepository.save(Voucher.builder()
                    .code("CHAOBANMOI")
                    .name("Chào mừng bạn mới - Giảm ngay 10%")
                    .discountPercent(10)
                    .maxDiscountAmount(new BigDecimal("100000"))
                    .minOrderAmount(new BigDecimal("200000"))
                    .startDate(LocalDate.now().minusDays(5))
                    .endDate(LocalDate.now().plusMonths(2))
                    .usageLimit(500)
                    .usedCount(12)
                    .active(true)
                    .build());

            voucherRepository.save(Voucher.builder()
                    .code("FREESHIP")
                    .name("Ưu đãi giảm giá 15% cho đơn từ 500k")
                    .discountPercent(15)
                    .maxDiscountAmount(new BigDecimal("150000"))
                    .minOrderAmount(new BigDecimal("500000"))
                    .startDate(LocalDate.now().minusDays(2))
                    .endDate(LocalDate.now().plusMonths(1))
                    .usageLimit(200)
                    .usedCount(5)
                    .active(true)
                    .build());

            // 6. Reviews
            reviewRepository.save(Review.builder()
                    .user(customer)
                    .product(p1)
                    .rating(5)
                    .comment("Táo rất tươi và giòn ngọt, vỏ mỏng bóng đẹp! Giao hàng nhanh và đóng gói bọc xốp rất cẩn thận.")
                    .build());

            reviewRepository.save(Review.builder()
                    .user(customer)
                    .product(p6)
                    .rating(5)
                    .comment("Giỏ quà rất sang trọng và đẹp mắt, trái cây đều quả và tươi rói. Đem đi biếu sinh nhật ai cũng khen!")
                    .build());

            log.info(">>> Khởi tạo dữ liệu mẫu hoàn tất thành công!");
        }
    }
}

