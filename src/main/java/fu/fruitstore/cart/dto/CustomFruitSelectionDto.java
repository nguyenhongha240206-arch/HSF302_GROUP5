package fu.fruitstore.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomFruitSelectionDto {
    private Long productId;
    private String productName;
    private String productCode;
    private String unit;
    private BigDecimal unitPrice;
    private int quantity;
    private String imageUrl;

    public BigDecimal getSubTotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

