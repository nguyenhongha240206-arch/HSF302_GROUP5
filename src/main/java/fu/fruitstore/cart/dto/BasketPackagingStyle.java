package fu.fruitstore.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketPackagingStyle {
    private Long id;
    private String name;
    private String material;
    private BigDecimal price;
    private String imageUrl;
    private String description;
    private String capacity;

    public static List<BasketPackagingStyle> getDefaultStyles() {
        return Arrays.asList(
            BasketPackagingStyle.builder()
                .id(1L)
                .name("Giỏ Mây Tre Đan Truyền Thống")
                .material("Mây tre tự nhiên đan thủ công")
                .price(new BigDecimal("80000"))
                .imageUrl("https://images.unsplash.com/photo-1543083477-4f785aeafaa9?auto=format&fit=crop&w=400&q=80")
                .description("Thiết kế mộc mạc, quai xách chắc chắn, lót rơm giấy và bọc màng co định hình hoa quả.")
                .capacity("Sức chứa: 3 - 5 kg hoa quả")
                .build(),
            BasketPackagingStyle.builder()
                .id(2L)
                .name("Hộp Da Nắp Kính Hoàng Gia VIP")
                .material("Khung gỗ bọc da cao cấp, nắp kính trong suốt")
                .price(new BigDecimal("150000"))
                .imageUrl("https://images.unsplash.com/photo-1528825871115-3581a5387919?auto=format&fit=crop&w=400&q=80")
                .description("Sang trọng, đẳng cấp thượng lưu, chuyên dụng làm quà biếu đối tác, doanh nghiệp.")
                .capacity("Sức chứa: 4 - 7 kg hoa quả")
                .build(),
            BasketPackagingStyle.builder()
                .id(3L)
                .name("Khay Gỗ Thông Mộc Tự Nhiên")
                .material("Gỗ thông tự nhiên nguyên khối")
                .price(new BigDecimal("100000"))
                .imageUrl("https://images.unsplash.com/photo-1596363505729-4190a9506133?auto=format&fit=crop&w=400&q=80")
                .description("Phong cách Bắc Âu tinh tế, tối giản, bề mặt láng mịn tôn vinh vẻ đẹp tự nhiên của trái cây.")
                .capacity("Sức chứa: 3 - 5 kg hoa quả")
                .build(),
            BasketPackagingStyle.builder()
                .id(4L)
                .name("Hộp Quà Giấy Mỹ Thuật Eco")
                .material("Carton lạnh bồi giấy mỹ thuật ép kim")
                .price(new BigDecimal("50000"))
                .imageUrl("https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=400&q=80")
                .description("Gọn gàng, hiện đại, màu sắc trang nhã, quai xách lụa mềm mại.")
                .capacity("Sức chứa: 2 - 4 kg hoa quả")
                .build()
        );
    }

    public static BasketPackagingStyle getById(Long id) {
        return getDefaultStyles().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(getDefaultStyles().get(0));
    }
}

