package fu.fruitstore.order.repository;

import fu.fruitstore.order.entity.Order;
import fu.fruitstore.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.orderStatus = :status) AND " +
           "(:keyword IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "o.receiverPhone LIKE CONCAT('%', :keyword, '%'))")
    Page<Order> searchOrders(@Param("status") OrderStatus status,
                             @Param("keyword") String keyword,
                             Pageable pageable);

    long countByOrderStatus(OrderStatus orderStatus);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
    BigDecimal calculateTotalDeliveredRevenue();

    List<Order> findTop5ByOrderByCreatedAtDesc();
}

