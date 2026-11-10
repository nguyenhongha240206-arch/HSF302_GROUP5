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
import fu.fruitstore.promotion.entity.Banner;
import fu.fruitstore.promotion.entity.UserVoucher;
import fu.fruitstore.promotion.repository.BannerRepository;
import fu.fruitstore.promotion.repository.UserVoucherRepository;
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
import java.time.LocalDateTime;
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
    private final BannerRepository bannerRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info(">>> Đang kiểm tra và khởi tạo dữ liệu mẫu (Seeding Database)...");

        // 1. Roles (Chỉ 2 vai trò: Chủ shop / Admin và Khách hàng)
        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Chủ shop (Quản trị viên)").build()));
        Role roleCustomer = roleRepository.findByName("ROLE_CUSTOMER").orElseGet(() ->
                roleRepository.save(Role.builder().name("ROLE_CUSTOMER").description("Khách hàng").build()));

        // 2. Users
        if (!userRepository.existsByEmail("admin@fruitstore.com")) {
            userRepository.save(User.builder()
                    .fullName("Nguyễn Quản Trị")
                    .email("admin@fruitstore.com")
                    .password(passwordEncoder.encode("123456"))
                    .phoneNumber("0988111222")
                    .address("FPT University, Khu CNC Hòa Lạc, Hà Nội")
                    .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80")
                    .enabled(true)
                    .roles(new HashSet<>(Set.of(roleAdmin)))
                    .build());
        } else {
            userRepository.findByEmail("admin@fruitstore.com").ifPresent(u -> {
                u.getRoles().clear();
                u.getRoles().add(roleAdmin);
                if (u.getFullName().contains("?") || (u.getAddress() != null && u.getAddress().contains("?"))) {
                    u.setFullName("Nguyễn Quản Trị");
                    u.setAddress("FPT University, Khu CNC Hòa Lạc, Hà Nội");
                }
                userRepository.save(u);
            });
        }

        // Clean up legacy staff accounts if present
        userRepository.findByEmail("staff@fruitstore.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("staff.demo@fruitstore.com").ifPresent(userRepository::delete);

        User customer = userRepository.findByEmail("customer@fruitstore.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName("Nguyễn Hồng Hà")
                        .email("customer@fruitstore.com")
                        .password(passwordEncoder.encode("123456"))
                        .phoneNumber("0912345678")
                        .address("Cầu Giấy, Hà Nội")
                        .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80")
                        .enabled(true)
                        .roles(new HashSet<>(Set.of(roleCustomer)))
                        .build())
        );
        if (customer.getFullName().contains("?") || (customer.getAddress() != null && customer.getAddress().contains("?"))) {
            customer.setFullName("Nguyễn Hồng Hà");
            customer.setAddress("Cầu Giấy, Hà Nội");
            userRepository.save(customer);
        }

        // 3. Categories
        Category catImport = categoryRepository.findByCode("CAT_IMPORT").orElseGet(() ->
                categoryRepository.save(Category.builder()
                        .name("Trái cây nhập khẩu")
                        .code("CAT_IMPORT")
                        .description("Các loại trái cây cao cấp nhập khẩu đường hàng không từ Mỹ, Úc, New Zealand, Nhật, Hàn")
                        .imageUrl("https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=600&q=80")
                        .displayOrder(1)
                        .active(true)
                        .build()));

        Category catLocal = categoryRepository.findByCode("CAT_LOCAL").orElseGet(() ->
                categoryRepository.save(Category.builder()
                        .name("Trái cây đặc sản Việt")
                        .code("CAT_LOCAL")
                        .description("Hoa quả đặc sản ba miền Việt Nam đạt chuẩn VietGAP, GlobalGAP tươi ngon")
                        .imageUrl("https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=600&q=80")
                        .displayOrder(2)
                        .active(true)
                        .build()));

        Category catGift = categoryRepository.findByCode("CAT_GIFT").orElseGet(() ->
                categoryRepository.save(Category.builder()
                        .name("Giỏ quà trái cây cao cấp")
                        .code("CAT_GIFT")
                        .description("Giỏ quà hoa quả trang trọng kèm nơ lụa và hoa tươi thích hợp biếu tặng, chúc mừng, dạm ngõ")
                        .imageUrl("https://images.unsplash.com/photo-1543083477-4f785aeafaa9?auto=format&fit=crop&w=600&q=80")
                        .displayOrder(3)
                        .active(true)
                        .build()));

        Category catDried = categoryRepository.findByCode("CAT_DRIED").orElseGet(() ->
                categoryRepository.save(Category.builder()
                        .name("Trái cây sấy khô & Chế biến")
                        .code("CAT_DRIED")
                        .description("Hoa quả sấy dẻo, sấy thăng hoa giữ nguyên hương vị và dinh dưỡng tự nhiên")
                        .imageUrl("https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&w=600&q=80")
                        .displayOrder(4)
                        .active(true)
                        .build()));

        // 4. Products (Trái cây nhập khẩu, Đặc sản Việt, Giỏ quà, Sấy khô)
        seedProduct("FRUIT_ENVY_NZ", "Táo Envy New Zealand Size 70",
                "Táo Envy New Zealand nhập khẩu chính ngạch, thịt giòn tan, vị ngọt đậm đà và hương thơm ngát tự nhiên.",
                "220000", "250000", 85, "kg", "New Zealand",
                "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catImport, true);

        seedProduct("FRUIT_MUSCAT_JP", "Nho mẫu đơn Shine Muscat Nhật",
                "Nho mẫu đơn Shine Muscat chuẩn hàng bay xách tay, quả to bóng mịn, không hạt, thơm hương sữa ngào ngạt.",
                "850000", "950000", 30, "hộp", "Nhật Bản",
                "https://images.unsplash.com/photo-1596363505729-4190a9506133?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catImport, true);

        seedProduct("FRUIT_FINGER_AU", "Nho ngón tay Midnight Beauty Úc",
                "Nho đen không hạt ngón tay Úc hình dáng thon dài độc đáo, thịt quả giòn rụm, vị ngọt thanh đậm đà.",
                "260000", "290000", 45, "kg", "Úc",
                "https://images.unsplash.com/photo-1537640538966-79f369143f8f?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catImport, false);

        seedProduct("FRUIT_CARA_AU", "Cam Cara Cara ruột đỏ Úc",
                "Cam không hạt ruột đỏ Cara ruột đỏ mọng nước, hàm lượng vitamin C cực cao, ngọt thanh mát.",
                "135000", "160000", 120, "kg", "Úc",
                "https://images.unsplash.com/photo-1582979512210-99b6a53386f9?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catImport, false);

        seedProduct("FRUIT_NAVEL_AU", "Cam vàng Navel Úc không hạt",
                "Cam vàng Navel vỏ mỏng dễ bóc, múi cam vàng mọng nước, thơm lừng vị ngọt tươi mát.",
                "115000", "135000", 95, "kg", "Úc",
                "https://images.unsplash.com/photo-1611080626919-7cf5a9dbab5b?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catImport, false);

        seedProduct("FRUIT_CHERRY_US", "Cherry đỏ Mỹ Size 8.5 chuẩn bay",
                "Cherry Washington đỏ thẫm cuống xanh tươi rói, quả chắc nịch, thịt ngọt đậm giòn tan đẳng cấp trái cây thượng hạng.",
                "490000", "560000", 40, "hộp", "Mỹ",
                "https://images.unsplash.com/photo-1528825871115-3581a5387919?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catImport, true);

        seedProduct("FRUIT_STRAW_KR", "Dâu tây Hàn Quốc Seolhyang cao cấp",
                "Dâu giống Seolhyang nổi tiếng xứ kim chi, quả đỏ mọng hình nón hoàn hảo, vị ngọt ngào và mùi thơm quyến rũ đặc trưng.",
                "350000", "390000", 25, "hộp", "Hàn Quốc",
                "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catImport, true);

        seedProduct("FRUIT_KIWI_NZ", "Kiwi vàng Zespri SunGold New Zealand",
                "Kiwi vàng Zespri ruột vàng ươm mọng nước, giàu vitamin C gấp 3 lần cam, vị chua ngọt cân bằng tinh tế.",
                "210000", "240000", 70, "kg", "New Zealand",
                "https://images.unsplash.com/photo-1585059895524-72359e06133a?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catImport, true);

        seedProduct("FRUIT_PEAR_KR", "Lê nâu Hàn Quốc Singold quả to giòn",
                "Lê nâu Hàn Quốc chuẩn xuất khẩu quả to tròn đều, vỏ nâu mỏng tang, thịt trắng giòn nhiều nước ngọt thanh tao.",
                "145000", "170000", 60, "kg", "Hàn Quốc",
                "https://images.unsplash.com/photo-1615484477778-ca3b77940c25?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catImport, false);

        seedProduct("FRUIT_BLUEBERRY_US", "Việt quất tươi hữu cơ Driscoll's Mỹ",
                "Việt quất quả chắc mẩy phủ phấn trắng tự nhiên, giàu chất chống oxy hóa anthocyanin tốt cho sức khỏe và sắc đẹp.",
                "165000", "195000", 50, "hộp", "Mỹ",
                "https://images.unsplash.com/photo-1498557850523-fd3d118b962e?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catImport, false);

        // Đặc sản Việt Nam
        seedProduct("FRUIT_XOAI_HL", "Xoài Cát Hòa Lộc Tiền Giang loại 1",
                "Xoài cát Hòa Lộc chính gốc Tiền Giang, thịt dày mịn, không xơ, hương thơm nồng nàn vị ngọt lịm.",
                "110000", "130000", 65, "kg", "Việt Nam",
                "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catLocal, true);

        seedProduct("FRUIT_BUOI_BT", "Bưởi da xanh Bến Tre ruột hồng",
                "Bưởi da xanh loại 1 cành lá sum suê, tép bưởi mọng nước, giòn ráo không hạt vị chua ngọt cân bằng.",
                "85000", "95000", 50, "quả", "Việt Nam",
                "https://images.unsplash.com/photo-1577234286642-fc512a5f8f11?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catLocal, false);

        seedProduct("FRUIT_SAURIENG_RI6", "Sầu riêng Ri6 cơm vàng hạt lép Đắk Lắk",
                "Sầu riêng Ri6 chín cây tự nhiên, cơm vàng óng ả dày cùi béo ngậy hạt lép, hương thơm quyến rũ nức mũi.",
                "160000", "185000", 35, "kg", "Việt Nam",
                "https://images.unsplash.com/photo-1587132137056-bfbf0166836e?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catLocal, true);

        seedProduct("FRUIT_DUALUOI_HL", "Dưa lưới Huỳnh Long ruột cam ngọt mát",
                "Dưa lưới trồng nhà màng công nghệ cao, vân lưới dày đều đẹp, ruột cam giòn ngọt lịm hương thơm mát.",
                "95000", "115000", 55, "kg", "Việt Nam",
                "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catLocal, false);

        seedProduct("FRUIT_MANGCUT_LT", "Măng cụt Lái Thiêu chuẩn VietGAP",
                "Nữ hoàng trái cây nhiệt đới Lái Thiêu vỏ mềm mỏng, múi trắng nõn mọng nước, vị chua ngọt thanh tao.",
                "125000", "150000", 40, "kg", "Việt Nam",
                "https://images.unsplash.com/photo-1546548970-71785318a17b?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catLocal, false);

        seedProduct("FRUIT_THANHLONG_BT", "Thanh long ruột đỏ Bình Thuận xuất khẩu",
                "Thanh long ruột đỏ đậm tự nhiên, ngọt đậm thơm ngon, giàu chất xơ và chất chống oxy hóa tự nhiên.",
                "45000", "55000", 80, "kg", "Việt Nam",
                "https://images.unsplash.com/photo-1527324688151-0e627063f2b1?auto=format&fit=crop&w=600&q=80",
                ProductType.FRUIT, catLocal, false);

        // Giỏ quà trái cây cao cấp
        seedProduct("GIFT_NGUPHUC_01", "Giỏ Trái Cây Ngũ Phúc Đoàn Viên",
                "Thiết kế giỏ mây tre đan cao cấp, kết hợp Táo Envy, Nho mẫu đơn, Cam Cara, Lê Hàn Quốc và Kiwi vàng. Kèm hoa lá tươi, ruy băng sang trọng.",
                "1250000", "1500000", 15, "giỏ", "Việt Nam / Nhập khẩu",
                "https://images.unsplash.com/photo-1543083477-4f785aeafaa9?auto=format&fit=crop&w=600&q=80",
                ProductType.GIFT_BASKET, catGift, true);

        seedProduct("GIFT_VIP_BIZ", "Hộp Quà Trái Cây Doanh Nghiệp VIP",
                "Hộp quà nắp kính ép nhũ sang trọng, tuyển chọn Cherry Mỹ, Dâu tây Hàn Quốc, Nho xanh không hạt. Đẳng cấp quà biếu đối tác.",
                "1850000", "2100000", 8, "hộp", "Nhập khẩu",
                "https://images.unsplash.com/photo-1528825871115-3581a5387919?auto=format&fit=crop&w=600&q=80",
                ProductType.GIFT_BASKET, catGift, true);

        seedProduct("GIFT_DAMNGO_02", "Giỏ Quà Hoa Quả Dạm Ngõ Thắm Duyên",
                "Mẫu giỏ quà cưới hỏi, dạm ngõ thiết kế thắt nơ lụa đỏ song hỷ, kết hợp nho đen không hạt, táo đỏ Ruby, cam vàng và dưa lưới.",
                "1600000", "1850000", 12, "giỏ", "Nhập khẩu & Đặc sản",
                "https://images.unsplash.com/photo-1519996529931-28324d5a630e?auto=format&fit=crop&w=600&q=80",
                ProductType.GIFT_BASKET, catGift, true);

        seedProduct("GIFT_LEPHAT_03", "Giỏ Quà Trái Cây Thắp Hương Lễ Phật",
                "Tuyển chọn các quả tươi cứng cáp đều đẹp: Bưởi da xanh cành lá, Táo đỏ, Cam vàng, Xoài cát dâng hương ban thờ, tổ tiên, lễ chùa.",
                "750000", "880000", 20, "giỏ", "Nhập khẩu & Việt Nam",
                "https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=600&q=80",
                ProductType.GIFT_BASKET, catGift, false);

        // Trái cây sấy khô & chế biến
        seedProduct("DRIED_XOAI_DEO", "Xoài sấy dẻo xuất khẩu loại 1",
                "Xoài cát sấy dẻo công nghệ lạnh, không chất bảo quản, giữ nguyên vị chua ngọt tự nhiên dẻo thơm.",
                "75000", "90000", 150, "gói", "Việt Nam",
                "https://images.unsplash.com/photo-1596547609652-9cf5d8d76921?auto=format&fit=crop&w=600&q=80",
                ProductType.DRIED_FRUIT, catDried, false);

        seedProduct("DRIED_MIT_DALAT", "Mít sấy giòn nguyên miếng Dalat",
                "Mít nghệ tươi sấy chân không giòn rụm, màu vàng óng tự nhiên, không đường hóa học, thơm lừng vị mít chín.",
                "60000", "75000", 130, "gói", "Việt Nam",
                "https://images.unsplash.com/photo-1601039641847-7857b994d704?auto=format&fit=crop&w=600&q=80",
                ProductType.DRIED_FRUIT, catDried, false);

        seedProduct("DRIED_STRAW_FREEZE", "Dâu tây sấy thăng hoa giữ trọn vitamin",
                "Dâu tây Đà Lạt sấy thăng hoa công nghệ Nhật Bản, giữ nguyên hình dáng quả, vị chua ngọt giòn xốp tan trong miệng.",
                "115000", "135000", 80, "hộp", "Việt Nam",
                "https://images.unsplash.com/photo-1518492104633-130d0cc84637?auto=format&fit=crop&w=600&q=80",
                ProductType.DRIED_FRUIT, catDried, false);

        seedProduct("DRIED_MACCA_DL", "Hạt macca sấy nứt vỏ Đắk Lắk VIP",
                "Hạt macca loại 1 nứt vỏ tự nhiên kèm dụng cụ tách, nhân trắng ngà béo ngậy ngọt bùi dinh dưỡng cho cả gia đình.",
                "140000", "165000", 90, "hộp", "Việt Nam",
                "https://images.unsplash.com/photo-1509358271058-acd22cc93898?auto=format&fit=crop&w=600&q=80",
                ProductType.DRIED_FRUIT, catDried, false);

        // 5. Vouchers
        seedVoucher("CHAOBANMOI", "Chào mừng bạn mới - Giảm ngay 10%", 10,
                new BigDecimal("100000"), new BigDecimal("200000"), 500, 12);
        seedVoucher("FREESHIP", "Ưu đãi giảm giá 15% cho đơn từ 500k", 15,
                new BigDecimal("150000"), new BigDecimal("500000"), 200, 5);
        seedVoucher("TRIAN50K", "Tri ân khách hàng thân thiết - Giảm 20%", 20,
                new BigDecimal("50000"), new BigDecimal("250000"), 300, 8);

        // 6. Reviews
        if (reviewRepository.count() == 0) {
            productRepository.findByCode("FRUIT_ENVY_NZ").ifPresent(prod ->
                    reviewRepository.save(Review.builder()
                            .user(customer)
                            .product(prod)
                            .rating(5)
                            .comment("Táo rất tươi và giòn ngọt, vỏ mỏng bóng đẹp! Giao hàng nhanh và đóng gói bọc xốp rất cẩn thận.")
                            .build())
            );

            productRepository.findByCode("GIFT_NGUPHUC_01").ifPresent(prod ->
                    reviewRepository.save(Review.builder()
                            .user(customer)
                            .product(prod)
                            .rating(5)
                            .comment("Giỏ quà rất sang trọng và đẹp mắt, trái cây đều quả và tươi rói. Đem đi biếu sinh nhật ai cũng khen!")
                            .build())
            );
        }

        // 7. Banners (Module 3 - HaNH UC-14)
        if (bannerRepository.count() == 0) {
            bannerRepository.save(Banner.builder()
                    .title("Lễ Hội Hoa Quả Mùa Hè - Tươi Ngon Giòn Ngọt")
                    .subtitle("Chiết khấu lên tới 25% cho tất cả các dòng Cherry Mỹ, Nho mẫu đơn & Táo Envy New Zealand đạt chuẩn GlobalGAP.")
                    .imageUrl("https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=1200&q=80")
                    .targetUrl("/catalog")
                    .position("HERO_SLIDER")
                    .displayOrder(1)
                    .startDate(LocalDate.now().minusDays(5))
                    .endDate(LocalDate.now().plusMonths(2))
                    .active(true)
                    .build());

            bannerRepository.save(Banner.builder()
                    .title("Thiết Kế Giỏ Quà Hoa Quả Độc Quyền Theo Yêu Cầu")
                    .subtitle("Tự tay chọn mẫu giỏ mây tre đan, phối các loại trái cây thượng hạng, tặng kèm nơ lụa & thiệp mừng cá nhân hóa.")
                    .imageUrl("https://images.unsplash.com/photo-1546548970-71785318a17b?auto=format&fit=crop&w=1200&q=80")
                    .targetUrl("/custom-basket")
                    .position("HERO_SLIDER")
                    .displayOrder(2)
                    .startDate(LocalDate.now().minusDays(5))
                    .endDate(LocalDate.now().plusMonths(3))
                    .active(true)
                    .build());

            bannerRepository.save(Banner.builder()
                    .title("Đặc Sản Trái Cây Ba Miền Chuẩn VietGAP")
                    .subtitle("Sầu riêng Ri6 chín cây, Bơ sáp 034 Đắk Lắk, Xoài cát Hòa Lộc thu hái trực tiếp từ nhà vườn uy tín.")
                    .imageUrl("https://images.unsplash.com/photo-1587049352846-4a222e784d38?auto=format&fit=crop&w=1200&q=80")
                    .targetUrl("/catalog?categoryId=2")
                    .position("HERO_SLIDER")
                    .displayOrder(3)
                    .startDate(LocalDate.now().minusDays(5))
                    .endDate(LocalDate.now().plusMonths(2))
                    .active(true)
                    .build());
        }

        // 8. Ví Voucher Khách Hàng (Module 3 - HaNH UC-15)
        if (userVoucherRepository.count() == 0) {
            voucherRepository.findByCodeIgnoreCaseAndActiveTrue("CHAOBANMOI").ifPresent(v -> {
                userVoucherRepository.save(UserVoucher.builder()
                        .user(customer)
                        .voucher(v)
                        .collectedAt(LocalDateTime.now())
                        .isUsed(false)
                        .build());
            });
            voucherRepository.findByCodeIgnoreCaseAndActiveTrue("TRIAN50K").ifPresent(v -> {
                userVoucherRepository.save(UserVoucher.builder()
                        .user(customer)
                        .voucher(v)
                        .collectedAt(LocalDateTime.now().minusDays(1))
                        .isUsed(false)
                        .build());
            });
        }

        log.info(">>> Khởi tạo dữ liệu mẫu hoàn tất thành công! Tổng số sản phẩm hiện có: {}", productRepository.count());
    }

    private void seedProduct(String code, String name, String description,
                             String price, String originalPrice, int stock,
                             String unit, String origin, String imageUrl,
                             ProductType type, Category category, boolean featured) {
        if (!productRepository.existsByCode(code)) {
            productRepository.save(Product.builder()
                    .code(code)
                    .name(name)
                    .description(description)
                    .price(new BigDecimal(price))
                    .originalPrice(originalPrice != null ? new BigDecimal(originalPrice) : null)
                    .stockQuantity(stock)
                    .unit(unit)
                    .origin(origin)
                    .imageUrl(imageUrl)
                    .productType(type)
                    .category(category)
                    .featured(featured)
                    .active(true)
                    .build());
        }
    }

    private void seedVoucher(String code, String name, int discountPercent,
                             BigDecimal maxDiscount, BigDecimal minOrderAmount,
                             int usageLimit, int usedCount) {
        if (!voucherRepository.existsByCodeIgnoreCase(code)) {
            voucherRepository.save(Voucher.builder()
                    .code(code)
                    .name(name)
                    .discountPercent(discountPercent)
                    .maxDiscountAmount(maxDiscount)
                    .minOrderAmount(minOrderAmount)
                    .startDate(LocalDate.now().minusDays(5))
                    .endDate(LocalDate.now().plusMonths(2))
                    .usageLimit(usageLimit)
                    .usedCount(usedCount)
                    .active(true)
                    .build());
        }
    }
}
