package fu.fruitstore.order.entity;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.shared.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = 0")
public class Order extends BaseEntity {

    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderNumber;

    // The current database has both the legacy order_code and order_number columns.
    // Keep both populated until the schema is consolidated.
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumberCompat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 300)
    private String shippingAddress;

    @Column(length = 500)
    private String note;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Builder.Default
    @Column(precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal finalAmount;

    @Column(length = 50)
    private String voucherCode;

    @Column(length = 50)
    private String deliveryTimeType; // EXPRESS_2H (Giao gấp 2h), SCHEDULED (Hẹn giờ)

    @Column(length = 100)
    private String deliveryTimeSlot; // Khung giờ: 08:00 - 11:30, 13:30 - 17:30, 18:00 - 21:00

    @Column(length = 200)
    private String packagingRequirement; // Tiêu chuẩn quả: Quả chín ăn ngay, Quả cứng thắp hương, Bọc lưới xốp...

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public void addOrderDetail(OrderDetail detail) {
        orderDetails.add(detail);
        detail.setOrder(this);
    }

    @PrePersist
    @PreUpdate
    private void syncOrderNumberColumns() {
        orderNumberCompat = orderNumber;
    }
}
