package fu.fruitstore.inventory.entity;

import fu.fruitstore.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "loss_slip_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LossSlipDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loss_slip_id", nullable = false)
    private LossSlip lossSlip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LossReason lossReason = LossReason.SPOILED;

    @Column(length = 255)
    private String note;

    public BigDecimal getSubTotal() {
        if (costPrice == null) return BigDecimal.ZERO;
        return costPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
