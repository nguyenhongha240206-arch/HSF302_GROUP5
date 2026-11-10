package fu.fruitstore.promotion.entity;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.shared.common.BaseEntity;
import fu.fruitstore.voucher.entity.Voucher;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = 0")
public class UserVoucher extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @Column(nullable = false)
    private LocalDateTime collectedAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean isUsed = false;

    private LocalDateTime usedAt;
}

