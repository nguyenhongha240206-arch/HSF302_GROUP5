package fu.fruitstore.order.controller;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.services.UserService;
import fu.fruitstore.cart.dto.CartDto;
import fu.fruitstore.cart.services.CartService;
import fu.fruitstore.order.dto.CheckoutDto;
import fu.fruitstore.order.entity.Order;
import fu.fruitstore.order.services.OrderService;
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

import java.util.List;

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
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Mã đơn của bạn là: " + order.getOrderNumber());
            return "redirect:/orders/" + order.getOrderNumber();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("cart", cart);
            return "store/checkout";
        }
    }

    @GetMapping("/orders/history")
    @PreAuthorize("isAuthenticated()")
    public String orderHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Order> orders = orderService.getOrdersByUser(userDetails.getUsername());
        model.addAttribute("orders", orders);
        return "store/order-history";
    }

    @GetMapping("/orders/{orderNumber}")
    public String orderDetail(@PathVariable("orderNumber") String orderNumber, Model model) {
        Order order = orderService.getByOrderNumber(orderNumber);
        model.addAttribute("order", order);
        return "store/order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public String cancelOrder(@PathVariable("id") Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders/history";
    }
}

