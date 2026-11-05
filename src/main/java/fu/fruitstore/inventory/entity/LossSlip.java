package fu.fruitstore.inventory.entity;

import fu.fruitstore.shared.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loss_slips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = 0")
public class LossSlip extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String slipCode;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime slipDate = LocalDateTime.now();

    @Column(length = 500)
    private String note;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalLossAmount = BigDecimal.ZERO;

    @Builder.Default
    @OneToMany(mappedBy = "lossSlip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LossSlipDetail> details = new ArrayList<>();

    public void addDetail(LossSlipDetail detail) {
        details.add(detail);
        detail.setLossSlip(this);
    }
}

