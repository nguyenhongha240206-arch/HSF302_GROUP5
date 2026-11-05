package fu.fruitstore.order.services;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.services.UserService;
import fu.fruitstore.cart.dto.CartDto;
import fu.fruitstore.cart.dto.CartItemDto;
import fu.fruitstore.cart.services.CartService;
import fu.fruitstore.order.dto.CheckoutDto;
import fu.fruitstore.order.entity.*;
import fu.fruitstore.order.repository.OrderRepository;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.services.ProductService;
import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import fu.fruitstore.voucher.services.VoucherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final VoucherService voucherService;

    @Override
    @Transactional
    public Order placeOrder(String userEmail, CheckoutDto checkoutDto, HttpSession session) {
        CartDto cart = cartService.getCart(session);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng của bạn đang trống! Vui lòng chọn sản phẩm trước khi đặt.");
        }

        User user = null;
        if (userEmail != null && !userEmail.isBlank() && !"anonymousUser".equals(userEmail)) {
            user = userService.findByEmail(userEmail);
        }

        // Generate unique order number
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = 1000 + new Random().nextInt(9000);
        String orderNumber = "FS-" + datePart + "-" + randomPart;

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .receiverName(checkoutDto.getReceiverName())
                .receiverPhone(checkoutDto.getReceiverPhone())
                .shippingAddress(checkoutDto.getShippingAddress())
                .note(checkoutDto.getNote())
                .totalAmount(cart.getTotalAmount())
                .discountAmount(cart.getDiscountAmount())
                .finalAmount(cart.getFinalAmount())
                .voucherCode(cart.getVoucherCode())
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(checkoutDto.getPaymentMethod())
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        // Process details and deduct stock
        for (CartItemDto item : cart.getItems()) {
            Product product = productService.getById(item.getProductId());
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho!");
            }

            OrderDetail detail = OrderDetail.builder()
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(item.getQuantity())
                    .subTotal(item.getSubTotal())
                    .build();

            order.addOrderDetail(detail);

            // Deduct inventory
            productService.adjustStock(product.getId(), -item.getQuantity());
        }

        Order savedOrder = orderRepository.save(order);

        // Increment voucher count if used
        if (cart.getVoucherCode() != null) {
            voucherService.incrementUsage(cart.getVoucherCode());
        }

        // Clear shopping cart
        cartService.clearCart(session);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã đơn hàng: " + orderNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(String userEmail) {
        User user = userService.findByEmail(userEmail);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(OrderStatus status, String keyword, Pageable pageable) {
        return orderRepository.searchOrders(status, keyword, pageable);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getById(orderId);
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Không thể cập nhật đơn hàng đã bị hủy!");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            // Restore inventory when cancelled by admin
            for (OrderDetail detail : order.getOrderDetails()) {
                productService.adjustStock(detail.getProduct().getId(), detail.getQuantity());
            }
        }

        if (newStatus == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long orderId, PaymentStatus newStatus) {
        Order order = getById(orderId);
        order.setPaymentStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String userEmail) {
        Order order = getById(orderId);
        User user = userService.findByEmail(userEmail);

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền thao tác trên đơn hàng này!");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Chỉ có thể hủy đơn hàng khi trạng thái là 'Chờ xác nhận'!");
        }

        // Return stock
        for (OrderDetail detail : order.getOrderDetails()) {
            productService.adjustStock(detail.getProduct().getId(), detail.getQuantity());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalDeliveredRevenue() {
        return orderRepository.calculateTotalDeliveredRevenue();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByOrderStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getRecentOrders() {
        return orderRepository.findTop5ByOrderByCreatedAtDesc();
    }
}

