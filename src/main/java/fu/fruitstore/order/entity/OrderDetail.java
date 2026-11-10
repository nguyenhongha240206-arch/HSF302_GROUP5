package fu.fruitstore.order.entity;

import fu.fruitstore.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @Column(nullable = false, length = 150)
    private String productName;

    @Column(length = 1000)
    private String itemDetails; // Kiểu dáng giỏ, phụ kiện nơ, thiệp, thành phần quả mix

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subTotal;
    @OneToMany(
            mappedBy = "orderDetail",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderDetailFruit> fruitComponents = new ArrayList<>();

    public void addFruitComponent(Product product, int quantity) {
        OrderDetailFruit component =
                new OrderDetailFruit(this, product, quantity);

        fruitComponents.add(component);
    }
}

