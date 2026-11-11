package fu.fruitstore.cart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartDto {
    private List<CartItemDto> items = new ArrayList<>();
    private String voucherCode;
    private BigDecimal discountAmount = BigDecimal.ZERO;

    public int getTotalQuantity() {
        return items.stream().mapToInt(CartItemDto::getQuantity).sum();
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItemDto::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getFinalAmount() {
        BigDecimal total = getTotalAmount();
        if (discountAmount == null) return total;
        BigDecimal finalAmt = total.subtract(discountAmount);
        return finalAmt.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmt;
    }
}

