package fu.fruitstore.cart.controller;

import fu.fruitstore.cart.dto.CartDto;
import fu.fruitstore.cart.services.CartService;
import fu.fruitstore.promotion.entity.UserVoucher;
import fu.fruitstore.promotion.services.UserVoucherService;
import fu.fruitstore.voucher.entity.Voucher;
import fu.fruitstore.voucher.services.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final VoucherService voucherService;
    private final UserVoucherService userVoucherService;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails,
                           HttpSession session, Model model) {
        CartDto cart = cartService.getCart(session);
        model.addAttribute("cart", cart);

        List<Voucher> publicVouchers = voucherService.getActiveVouchers();
        model.addAttribute("availableVouchers", publicVouchers);

        if (userDetails != null) {
            List<UserVoucher> myVouchers = userVoucherService.getMyActiveVouchers(userDetails.getUsername());
            model.addAttribute("myWalletVouchers", myVouchers);

            // Tự động tìm voucher tốt nhất trong ví của khách hàng
            Optional<Voucher> best = userVoucherService.findBestVoucherForOrder(userDetails.getUsername(), cart.getTotalAmount());
            best.ifPresent(v -> {
                model.addAttribute("bestVoucher", v);
                model.addAttribute("bestDiscountAmount", v.calculateDiscount(cart.getTotalAmount()));
            });
        }

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

    /**
     * UC-12: REST API Áp dụng Voucher trả về JSON cập nhật trực tiếp trên UI giỏ hàng
     */
    @PostMapping("/voucher/api/apply")
    @ResponseBody
    public ResponseEntity<?> applyVoucherAjax(@RequestParam("voucherCode") String voucherCode,
                                              HttpSession session) {
        try {
            cartService.applyVoucher(session, voucherCode);
            CartDto cart = cartService.getCart(session);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Áp dụng voucher '" + voucherCode.trim().toUpperCase() + "' thành công!");
            response.put("voucherCode", cart.getVoucherCode());
            response.put("totalAmount", cart.getTotalAmount());
            response.put("discountAmount", cart.getDiscountAmount());
            response.put("finalAmount", cart.getFinalAmount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * UC-12: REST API Hủy áp dụng Voucher
     */
    @PostMapping("/voucher/api/remove")
    @ResponseBody
    public ResponseEntity<?> removeVoucherAjax(HttpSession session) {
        cartService.removeVoucher(session);
        CartDto cart = cartService.getCart(session);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã hủy mã giảm giá!");
        response.put("totalAmount", cart.getTotalAmount());
        response.put("discountAmount", BigDecimal.ZERO);
        response.put("finalAmount", cart.getFinalAmount());
        return ResponseEntity.ok(response);
    }
}
