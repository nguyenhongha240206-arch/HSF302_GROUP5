package fu.fruitstore.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellingFruitDto {
    private int rank;
    private Long productId;
    private String productName;
    private String productCode;
    private String categoryName;
    private String unit;
    private String imageUrl;
    private long totalQuantitySold;
    private BigDecimal totalRevenue;
}

