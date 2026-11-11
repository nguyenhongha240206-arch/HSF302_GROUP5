package fu.fruitstore.order.services;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.services.UserService;
import fu.fruitstore.cart.dto.CartDto;
import fu.fruitstore.cart.dto.CartItemDto;
import fu.fruitstore.cart.dto.CustomFruitSelectionDto;
import fu.fruitstore.cart.services.CartService;
import fu.fruitstore.order.dto.CheckoutDto;
import fu.fruitstore.order.entity.*;
import fu.fruitstore.order.repository.OrderRepository;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.services.ProductService;
import fu.fruitstore.promotion.services.UserVoucherService;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;
    private final CartService cartService;
    private final VoucherService voucherService;
    private final UserVoucherService userVoucherService;

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

        // Delivery time slot formatting
        String fullTimeSlot = checkoutDto.getDeliveryTimeSlot();
        if (checkoutDto.getDeliveryDate() != null && !checkoutDto.getDeliveryDate().isBlank()) {
            fullTimeSlot = fullTimeSlot + " (" + checkoutDto.getDeliveryDate() + ")";
        }

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .receiverName(checkoutDto.getReceiverName())
                .receiverPhone(checkoutDto.getReceiverPhone())
                .shippingAddress(checkoutDto.getShippingAddress())
                .deliveryTimeType(checkoutDto.getDeliveryTimeType())
                .deliveryTimeSlot(fullTimeSlot)
                .packagingRequirement(checkoutDto.getPackagingRequirement())
                .note(checkoutDto.getNote())
                .totalAmount(cart.getTotalAmount())
                .discountAmount(cart.getDiscountAmount())
                .finalAmount(cart.getFinalAmount())
                .voucherCode(cart.getVoucherCode())
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(checkoutDto.getPaymentMethod() != null ? checkoutDto.getPaymentMethod() : PaymentMethod.COD)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();


// Process order details and deduct stock
        for (CartItemDto item : cart.getItems()) {

            if (item.isCustomBasket()) {

                // Tạo OrderDetail trước để gắn các trái cây thành phần
                OrderDetail detail = OrderDetail.builder()
                        .product(null)
                        .productName(item.getProductName())
                        .itemDetails(item.getCustomBasketSummary())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subTotal(item.getSubTotal())
                        .build();

                // Lưu từng trái cây trong Custom Basket
                for (CustomFruitSelectionDto fruit : item.getCustomFruitItems()) {

                    Product fruitProduct =
                            productService.getById(fruit.getProductId());

                    int requiredQty = fruit.getQuantity() * item.getQuantity();

                    if (requiredQty <= 0) {
                        throw new BadRequestException(
                                "Số lượng trái cây trong giỏ quà không hợp lệ!"
                        );
                    }

                    // Trừ tồn kho bằng phương thức có khóa pessimistic
                    productService.adjustStock(
                            fruitProduct.getId(),
                            -requiredQty
                    );

                    // Lưu thành phần để sau này có thể hoàn kho
                    detail.addFruitComponent(fruitProduct, requiredQty);
                }

                order.addOrderDetail(detail);

            } else {

                // Regular fruit product
                Product product = productService.getById(item.getProductId());

                if (item.getQuantity() <= 0) {
                    throw new BadRequestException(
                            "Số lượng sản phẩm không hợp lệ!"
                    );
                }

                OrderDetail detail = OrderDetail.builder()
                        .product(product)
                        .productName(product.getName())
                        .itemDetails(
                                "ĐVT: " + product.getUnit()
                                        + " | Xuất xứ: " + product.getOrigin()
                        )
                        .unitPrice(product.getPrice())
                        .quantity(item.getQuantity())
                        .subTotal(item.getSubTotal())
                        .build();

                order.addOrderDetail(detail);

                // Deduct inventory
                productService.adjustStock(
                        product.getId(),
                        -item.getQuantity()
                );
            }
        }


        Order savedOrder = orderRepository.save(order);

        // Increment voucher count if used and mark as used in user's wallet
        if (cart.getVoucherCode() != null) {
            voucherService.incrementUsage(cart.getVoucherCode());
            if (userEmail != null) {
                userVoucherService.markVoucherAsUsed(userEmail, cart.getVoucherCode());
            }
        }

        // Clear shopping cart
        cartService.clearCart(session);

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getByOrderNumber(String orderNumber) {
        return orderRepository.findWithDetailsByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy mã đơn hàng: " + orderNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(String userEmail) {
        User user = userService.findByEmail(userEmail);
        return orderRepository.findWithDetailsByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(OrderStatus status, String keyword, Pageable pageable) {
        return orderRepository.searchOrders(status, keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn hàng với ID: " + orderId
                ));
        OrderStatus currentStatus = order.getOrderStatus();

        if (currentStatus == OrderStatus.CANCELLED
                || currentStatus == OrderStatus.DELIVERED) {
            throw new BadRequestException(
                    "Đơn hàng đã kết thúc, không thể cập nhật trạng thái!"
            );
        }

        boolean validTransition = switch (currentStatus) {
            case PENDING ->
                    newStatus == OrderStatus.CONFIRMED
                            || newStatus == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    newStatus == OrderStatus.SHIPPING;

            case SHIPPING ->
                    newStatus == OrderStatus.DELIVERED;

            default -> false;
        };

        if (!validTransition) {
            throw new BadRequestException(
                    "Không thể chuyển trạng thái từ "
                            + currentStatus.getDisplayName()
                            + " sang "
                            + newStatus.getDisplayName()
                            + "!"
            );
        }

        // Hoàn kho khi admin hủy đơn
        if (newStatus == OrderStatus.CANCELLED) {

            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                throw new BadRequestException(
                        "Đơn hàng đã thanh toán. Không thể hủy trực tiếp!"
                );
            }

            restoreOrderStock(order);
        }

        // COD chỉ được xem là đã thanh toán khi giao thành công
        if (newStatus == OrderStatus.DELIVERED
                && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long orderId, PaymentStatus newStatus) {
        Order order = getById(orderId);

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException(
                    "Đơn hàng đã thanh toán, không thể thay đổi trạng thái!"
            );
        }

        if (newStatus != PaymentStatus.PAID) {
            throw new BadRequestException(
                    "Chỉ được phép xác nhận thanh toán từ UNPAID sang PAID!"
            );
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException(
                    "Không thể xác nhận thanh toán cho đơn hàng đã hủy!"
            );
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);
    }




    @Override
    @Transactional
    public void cancelOrder(Long orderId, String userEmail) {

        // 1. Khóa đơn hàng trước khi xử lý
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn hàng với ID: " + orderId
                ));

        // 2. Kiểm tra người dùng
        User user = userService.findByEmail(userEmail);

        // 3. Kiểm tra quyền sở hữu đơn hàng
        if (order.getUser() == null
                || !order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(
                    "Bạn không có quyền hủy đơn hàng này!"
            );
        }

        // 4. Chỉ được hủy đơn PENDING
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                    "Chỉ có thể hủy đơn hàng khi đang chờ xác nhận!"
            );
        }

        // 5. Không cho hủy nếu đã thanh toán
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException(
                    "Đơn hàng đã thanh toán. Vui lòng liên hệ Admin để được hỗ trợ!"
            );
        }

        // 6. Hoàn kho sản phẩm thường và Custom Basket
        restoreOrderStock(order);

        // 7. Cập nhật trạng thái
        order.setOrderStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);
    }




    @Override
    @Transactional
    public void confirmDelivery(Long orderId, String userEmail) {
        Order order = getById(orderId);
        User user = userService.findByEmail(userEmail);

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền thao tác trên đơn hàng này!");
        }

        if (order.getOrderStatus() != OrderStatus.SHIPPING) {
            throw new BadRequestException(
                    "Chỉ có thể xác nhận khi đơn hàng đang được giao!"
            );
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void reorder(Long orderId, HttpSession session) {
        Order order = getById(orderId);
        int addedItemsCount = 0;
        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail.getProduct() != null && !detail.getProduct().isDeleted() && detail.getProduct().isActive()) {
                cartService.addToCart(session, detail.getProduct().getId(), detail.getQuantity());
                addedItemsCount++;
            }
        }
        if (addedItemsCount == 0) {
            throw new BadRequestException("Các sản phẩm trong đơn hàng này hiện không còn khả dụng để mua lại!");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<OrderStatus, Long> getOrderStatusCounts() {
        Map<OrderStatus, Long> counts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus st : OrderStatus.values()) {
            counts.put(st, orderRepository.countByOrderStatus(st));
        }
        return counts;
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
    public void restoreOrderStock(Order order) {

        for (OrderDetail detail : order.getOrderDetails()) {

            // 1. Hoàn kho sản phẩm thông thường
            if (detail.getProduct() != null) {
                productService.adjustStock(
                        detail.getProduct().getId(),
                        detail.getQuantity()
                );
            }

            // 2. Hoàn kho từng trái cây trong Custom Basket
            if (detail.getFruitComponents() != null) {
                for (OrderDetailFruit component : detail.getFruitComponents()) {

                    productService.adjustStock(
                            component.getProduct().getId(),
                            component.getQuantity()
                    );
                }
            }
        }
    }
}
