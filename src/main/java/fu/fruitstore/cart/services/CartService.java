package fu.fruitstore.cart.services;

import fu.fruitstore.cart.dto.CartDto;
import fu.fruitstore.cart.dto.CustomBasketRequestDto;
import jakarta.servlet.http.HttpSession;

public interface CartService {
    CartDto getCart(HttpSession session);
    void addToCart(HttpSession session, Long productId, int quantity);
    void addCustomBasketToCart(HttpSession session, CustomBasketRequestDto requestDto);
    void updateQuantity(HttpSession session, String itemId, int quantity);
    void removeFromCart(HttpSession session, String itemId);
    void applyVoucher(HttpSession session, String voucherCode);
    void removeVoucher(HttpSession session);
    void clearCart(HttpSession session);
}
