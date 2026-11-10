package fu.fruitstore.order.services;

import fu.fruitstore.order.dto.CheckoutDto;
import fu.fruitstore.order.entity.Order;
import fu.fruitstore.order.entity.OrderStatus;
import fu.fruitstore.order.entity.PaymentStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Order placeOrder(String userEmail, CheckoutDto checkoutDto, HttpSession session);
    Order getById(Long id);
    Order getByOrderNumber(String orderNumber);
    List<Order> getOrdersByUser(String userEmail);
    Page<Order> getAllOrders(OrderStatus status, String keyword, Pageable pageable);
    Page<Order> getOrdersByUser(Long userId, Pageable pageable);
    void updateOrderStatus(Long orderId, OrderStatus newStatus);
    void updatePaymentStatus(Long orderId, PaymentStatus newStatus);
    void cancelOrder(Long orderId, String userEmail);
    void confirmDelivery(Long orderId, String userEmail);
    void reorder(Long orderId, HttpSession session);
    Map<OrderStatus, Long> getOrderStatusCounts();
    BigDecimal getTotalDeliveredRevenue();
    long countByStatus(OrderStatus status);
    List<Order> getRecentOrders();
    void restoreOrderStock(Order order);
}

