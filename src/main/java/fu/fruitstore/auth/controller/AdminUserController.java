package fu.fruitstore.auth.controller;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.services.UserService;
import fu.fruitstore.order.entity.Order;
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
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size,
                            Model model) {
        Page<User> usersPage = userService.getAllUsers(keyword, PageRequest.of(page, size, Sort.by("id").descending()));
        model.addAttribute("usersPage", usersPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUserEmail", org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/users/list";
    }

    @GetMapping("/{id}/orders")
    public String orderHistory(@PathVariable("id") Long id,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        User user = userService.findById(id);
        Page<Order> ordersPage = orderService.getOrdersByUser(id,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("user", user);
        model.addAttribute("ordersPage", ordersPage);
        return "admin/users/order-history";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes,
                               org.springframework.security.core.Authentication authentication) {
        if (userService.findById(id).getEmail().equalsIgnoreCase(authentication.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự khóa tài khoản của mình.");
            return "redirect:/admin/users";
        }
        userService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái tài khoản thành công!");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes,
                             org.springframework.security.core.Authentication authentication) {
        if (userService.findById(id).getEmail().equalsIgnoreCase(authentication.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự xóa tài khoản của mình.");
            return "redirect:/admin/users";
        }
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản thành công!");
        return "redirect:/admin/users";
    }
}

