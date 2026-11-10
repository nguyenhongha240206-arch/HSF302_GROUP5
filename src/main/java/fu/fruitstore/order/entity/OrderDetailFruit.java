
package fu.fruitstore.order.entity;

import fu.fruitstore.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_detail_fruits")
@Getter
@Setter
@NoArgsConstructor
public class OrderDetailFruit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public OrderDetailFruit(OrderDetail orderDetail,
                            Product product,
                            Integer quantity) {
        this.orderDetail = orderDetail;
        this.product = product;
        this.quantity = quantity;
    }

}
