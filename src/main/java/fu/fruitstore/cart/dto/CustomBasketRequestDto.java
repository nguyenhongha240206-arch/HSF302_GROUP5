package fu.fruitstore.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomBasketRequestDto {
    @Builder.Default
    private String basketName = "Giỏ Quà Hoa Quả Tự Thiết Kế";

    private Long packagingStyleId;

    @Builder.Default
    private String ribbonColor = "Đỏ Nhung May Mắn";

    private String greetingCardMessage;

    @Builder.Default
    private List<CustomFruitSelectionDto> selectedFruits = new ArrayList<>();

    public BigDecimal calculateFruitsTotal() {
        if (selectedFruits == null) return BigDecimal.ZERO;
        return selectedFruits.stream()
                .map(CustomFruitSelectionDto::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateGrandTotal() {
        BasketPackagingStyle style = BasketPackagingStyle.getById(packagingStyleId);
        BigDecimal stylePrice = (style != null && style.getPrice() != null) ? style.getPrice() : BigDecimal.ZERO;
        return stylePrice.add(calculateFruitsTotal());
    }

    public String generateSummaryText() {
        BasketPackagingStyle style = BasketPackagingStyle.getById(packagingStyleId);
        StringBuilder sb = new StringBuilder();
        sb.append("Kiểu dáng: ").append(style != null ? style.getName() : "Mặc định");
        sb.append(" | Nơ: ").append(ribbonColor != null ? ribbonColor : "Đỏ nhung");
        if (greetingCardMessage != null && !greetingCardMessage.isBlank()) {
            sb.append(" | Thiệp: \"").append(greetingCardMessage.trim()).append("\"");
        }
        sb.append(" | Thành phần quả: ");
        if (selectedFruits != null && !selectedFruits.isEmpty()) {
            List<String> fruitSummaries = new ArrayList<>();
            for (CustomFruitSelectionDto f : selectedFruits) {
                if (f.getQuantity() > 0) {
                    fruitSummaries.add(f.getQuantity() + " " + f.getUnit() + " " + f.getProductName());
                }
            }
            sb.append(String.join(", ", fruitSummaries));
        }
        return sb.toString();
    }
}

