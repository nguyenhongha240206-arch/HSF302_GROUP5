package fu.fruitstore.promotion.entity;

import fu.fruitstore.shared.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = 0")
public class Banner extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String subtitle;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Column(length = 500)
    private String targetUrl;

    @Builder.Default
    @Column(length = 50)
    private String position = "HERO_SLIDER"; // HERO_SLIDER, PROMO_BANNER, SIDEBAR

    @Builder.Default
    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    public boolean isCurrentlyRunning() {
        if (!active) return false;
        LocalDate now = LocalDate.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
}

