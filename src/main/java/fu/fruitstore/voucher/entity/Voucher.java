package fu.fruitstore.voucher.entity;

import fu.fruitstore.shared.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = 0")
public class Voucher extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private Integer discountPercent; // e.g., 10 for 10%

    @Column(precision = 15, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Builder.Default
    @Column(nullable = false)
    private Integer usageLimit = 100;

    @Builder.Default
    @Column(nullable = false)
    private Integer usedCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public boolean isValid(BigDecimal orderAmount) {
        if (!active) return false;
        LocalDate now = LocalDate.now();
        if (now.isBefore(startDate) || now.isAfter(endDate)) return false;
        if (usedCount >= usageLimit) return false;
        if (minOrderAmount != null && orderAmount.compareTo(minOrderAmount) < 0) return false;
        return true;
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isValid(orderAmount)) return BigDecimal.ZERO;
        BigDecimal discount = orderAmount.multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100));
        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            return maxDiscountAmount;
        }
        return discount;
    }
}

