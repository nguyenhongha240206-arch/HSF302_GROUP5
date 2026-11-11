package fu.fruitstore.cart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItemDto {
    private String itemId; // Unique identifier in cart (e.g., P_1 or BASKET_xxxx)
    private Long productId; // Null for custom basket
    private String productName;
    private String productCode;
    private String imageUrl;
    private String unit;
    private BigDecimal unitPrice;
    private int quantity;

    @Builder.Default
    private String itemType = "REGULAR"; // REGULAR or CUSTOM_BASKET

    // Specialized fields for UC-09: Custom Gift Basket
    private String basketStyleName;
    private String ribbonColor;
    private String greetingCardMessage;
    private String customBasketSummary;

    @Builder.Default
    private List<CustomFruitSelectionDto> customFruitItems = new ArrayList<>();

    public BigDecimal getSubTotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean isCustomBasket() {
        return "CUSTOM_BASKET".equalsIgnoreCase(itemType);
    }
}
