package fu.fruitstore.cart.controller;

import fu.fruitstore.cart.dto.CartDto;
import fu.fruitstore.cart.services.CartService;
import fu.fruitstore.voucher.services.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

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
    public Object addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            HttpServletRequest request,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));
        try {
            cartService.addToCart(session, productId, quantity);
            CartDto cart = cartService.getCart(session);
            if (isAjax) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Đã thêm hoa quả vào giỏ hàng thành công!");
                response.put("totalQuantity", cart.getTotalQuantity());
                return ResponseEntity.ok(response);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm hoa quả vào giỏ hàng thành công!");
        } catch (Exception e) {
            if (isAjax) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(error);
            }
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null && !referer.isBlank() ? referer : "/cart");
    }

    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<?> addToCartApi(@RequestParam("productId") Long productId,
                                          @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                                          HttpSession session) {
        try {
            cartService.addToCart(session, productId, quantity);
            CartDto cart = cartService.getCart(session);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã thêm hoa quả vào giỏ hàng thành công!");
            response.put("totalQuantity", cart.getTotalQuantity());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/api/count")
    @ResponseBody
    public ResponseEntity<?> getCartCount(HttpSession session) {
        CartDto cart = cartService.getCart(session);
        return ResponseEntity.ok(Map.of("totalQuantity", cart.getTotalQuantity()));
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
