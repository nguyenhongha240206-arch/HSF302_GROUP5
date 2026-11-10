package fu.fruitstore.dashboard.controller;

import fu.fruitstore.auth.repository.UserRepository;
import fu.fruitstore.inventory.services.InventoryService;
import fu.fruitstore.order.entity.OrderStatus;
import fu.fruitstore.order.services.OrderService;
import fu.fruitstore.product.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final OrderService orderService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalRevenue", orderService.getTotalDeliveredRevenue());
        model.addAttribute("pendingOrdersCount", orderService.countByStatus(OrderStatus.PENDING));
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("lowStockCount", productService.countLowStock(10));
        model.addAttribute("totalLossAmount", inventoryService.getTotalLossAmount());

        model.addAttribute("recentOrders", orderService.getRecentOrders());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts(10));

        return "admin/dashboard";
    }
}

