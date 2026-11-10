package fu.fruitstore.order.controller;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.services.UserService;
import fu.fruitstore.cart.dto.CartDto;
import fu.fruitstore.cart.services.CartService;
import fu.fruitstore.order.dto.CheckoutDto;
import fu.fruitstore.order.entity.Order;
import fu.fruitstore.order.entity.OrderStatus;
import fu.fruitstore.order.entity.PaymentMethod;
import fu.fruitstore.order.services.OrderService;
import fu.fruitstore.review.dto.ReviewDto;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserService userService;

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails,
                               HttpSession session,
                               Model model) {
        CartDto cart = cartService.getCart(session);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        CheckoutDto checkoutDto = new CheckoutDto();
        checkoutDto.setPaymentMethod(PaymentMethod.COD);

        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            checkoutDto.setReceiverName(user.getFullName());
            checkoutDto.setReceiverPhone(user.getPhoneNumber());
            checkoutDto.setShippingAddress(user.getAddress());
        }

        model.addAttribute("checkoutDto", checkoutDto);
        model.addAttribute("cart", cart);
        return "store/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@AuthenticationPrincipal UserDetails userDetails,
                                  @Valid @ModelAttribute("checkoutDto") CheckoutDto checkoutDto,
                                  BindingResult bindingResult,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        CartDto cart = cartService.getCart(session);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cart", cart);
            return "store/checkout";
        }

        try {
            String userEmail = userDetails != null ? userDetails.getUsername() : null;
            Order order = orderService.placeOrder(userEmail, checkoutDto, session);

            if (order.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đặt hàng thành công! Vui lòng quét mã VietQR bên dưới để hoàn tất thanh toán cho đơn: " + order.getOrderNumber());
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đặt hàng thành công! Mã đơn của bạn là: " + order.getOrderNumber());
            }
            return "redirect:/orders/" + order.getOrderNumber();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("cart", cart);
            return "store/checkout";
        }
    }

    @GetMapping("/orders/history")
    @PreAuthorize("isAuthenticated()")
    public String orderHistory(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               Model model) {
        List<Order> orders = orderService.getOrdersByUser(userDetails.getUsername());

        if (status != null && !status.isBlank()) {
            try {
                OrderStatus filterStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orders.stream()
                        .filter(o -> o.getOrderStatus() == filterStatus)
                        .collect(Collectors.toList());
                model.addAttribute("selectedStatus", status.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (keyword != null && !keyword.isBlank()) {
            String searchKey = keyword.trim().toLowerCase();
            orders = orders.stream()
                    .filter(o -> o.getOrderNumber() != null && o.getOrderNumber().toLowerCase().contains(searchKey))
                    .collect(Collectors.toList());
            model.addAttribute("keyword", keyword.trim());
        }

        model.addAttribute("orders", orders);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "store/order-history";
    }

    @GetMapping("/orders/lookup")
    public String lookupOrder(@RequestParam("orderNumber") String orderNumber,
                              RedirectAttributes redirectAttributes) {
        if (orderNumber == null || orderNumber.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập mã đơn hàng cần tra cứu!");
            return "redirect:/orders/history";
        }
        try {
            Order order = orderService.getByOrderNumber(orderNumber.trim());
            return "redirect:/orders/" + order.getOrderNumber();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng với mã: " + orderNumber);
            return "redirect:/orders/history";
        }
    }

    @GetMapping("/orders/{orderNumber}")
    public String orderDetail(@PathVariable("orderNumber") String orderNumber, Model model) {
        Order order = orderService.getByOrderNumber(orderNumber);
        model.addAttribute("order", order);

        if (order.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            long amount = order.getFinalAmount() != null ? order.getFinalAmount().longValue() : 0L;
            String addInfo = URLEncoder.encode("THANHTOAN " + order.getOrderNumber(), StandardCharsets.UTF_8);
            String accountName = URLEncoder.encode("FRUIT STORE VIET NAM", StandardCharsets.UTF_8);
            String vietQrUrl = String.format(
                    "https://img.vietqr.io/image/MB-0388888888-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    amount, addInfo, accountName
            );
            model.addAttribute("vietQrUrl", vietQrUrl);
        }

        if (!model.containsAttribute("reviewDto")) {
            model.addAttribute("reviewDto", new ReviewDto());
        }

        return "store/order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public String cancelOrder(@PathVariable("id") Long id,
                              @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/orders/history";
    }

    @PostMapping("/orders/{id}/confirm-received")
    @PreAuthorize("isAuthenticated()")
    public String confirmReceived(@PathVariable("id") Long id,
                                  @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            orderService.confirmDelivery(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã xác nhận nhận hàng thành công! Đơn hàng đã hoàn tất.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/orders/history";
    }

    @PostMapping("/orders/{id}/reorder")
    public String reorder(@PathVariable("id") Long id,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        try {
            orderService.reorder(id, session);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm các mặt hàng vào giỏ hàng thành công!");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể mua lại đơn hàng: " + e.getMessage());
            return "redirect:/orders/history";
        }
    }
}