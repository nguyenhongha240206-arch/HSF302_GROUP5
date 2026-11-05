package fu.fruitstore.cart.services;

import fu.fruitstore.cart.dto.CartDto;
import jakarta.servlet.http.HttpSession;

public interface CartService {
    CartDto getCart(HttpSession session);
    void addToCart(HttpSession session, Long productId, int quantity);
    void updateQuantity(HttpSession session, Long productId, int quantity);
    void removeFromCart(HttpSession session, Long productId);
    void applyVoucher(HttpSession session, String voucherCode);
    void removeVoucher(HttpSession session);
    void clearCart(HttpSession session);
}

