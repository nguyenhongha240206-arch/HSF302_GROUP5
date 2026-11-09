package fu.fruitstore.cart.controller;

import fu.fruitstore.cart.dto.CartDto;
import fu.fruitstore.cart.services.CartService;
import fu.fruitstore.voucher.services.VoucherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final VoucherService voucherService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        CartDto cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("availableVouchers", voucherService.getActiveVouchers());
        return "store/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(session, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm hoa quả vào giỏ hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam(value = "itemId", required = false) String itemId,
                                 @RequestParam(value = "productId", required = false) Long productId,
                                 @RequestParam("quantity") int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            String targetId = itemId != null && !itemId.isBlank() ? itemId : ("P_" + productId);
            cartService.updateQuantity(session, targetId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam(value = "itemId", required = false) String itemId,
                                 @RequestParam(value = "productId", required = false) Long productId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        String targetId = itemId != null && !itemId.isBlank() ? itemId : ("P_" + productId);
        cartService.removeFromCart(session, targetId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng!");
        return "redirect:/cart";
    }

    @PostMapping("/voucher/apply")
    public String applyVoucher(@RequestParam("voucherCode") String voucherCode,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            cartService.applyVoucher(session, voucherCode);
            redirectAttributes.addFlashAttribute("successMessage", "Áp dụng mã giảm giá '" + voucherCode.trim().toUpperCase() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/voucher/remove")
    public String removeVoucher(HttpSession session, RedirectAttributes redirectAttributes) {
        cartService.removeVoucher(session);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy áp dụng mã giảm giá!");
        return "redirect:/cart";
    }
}
