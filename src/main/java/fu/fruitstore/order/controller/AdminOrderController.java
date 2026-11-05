package fu.fruitstore.order.controller;

import fu.fruitstore.order.entity.Order;
import fu.fruitstore.order.entity.OrderStatus;
import fu.fruitstore.order.entity.PaymentStatus;
import fu.fruitstore.order.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) OrderStatus status,
                             @RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             Model model) {
        Page<Order> orderPage = orderService.getAllOrders(status, keyword,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String viewDetail(@PathVariable("id") Long id, Model model) {
        Order order = orderService.getById(id);
        model.addAttribute("order", order);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        return "admin/orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable("id") Long id,
                               @RequestParam("status") OrderStatus status,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/payment")
    public String updatePaymentStatus(@PathVariable("id") Long id,
                                      @RequestParam("paymentStatus") PaymentStatus paymentStatus,
                                      RedirectAttributes redirectAttributes) {
        try {
            orderService.updatePaymentStatus(id, paymentStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thanh toán thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}

