package fu.fruitstore.review.entity;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.shared.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = 0")
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int rating; // 1 to 5 stars

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    // BỔ SUNG CHO UC-15: Lưu đường dẫn hình ảnh báo cáo quả dập nát / hư hỏng
    @Column(name = "image_url", length = 500)
    private String imageUrl;
}