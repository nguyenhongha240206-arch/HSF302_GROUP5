package fu.fruitstore.cart.services;

import fu.fruitstore.cart.dto.CartDto;
import fu.fruitstore.cart.dto.CartItemDto;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.services.ProductService;
import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.voucher.services.VoucherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private static final String CART_SESSION_KEY = "SHOPPING_CART";

    private final ProductService productService;
    private final VoucherService voucherService;

    @Override
    public CartDto getCart(HttpSession session) {
        CartDto cart = (CartDto) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new CartDto();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    @Override
    public void addToCart(HttpSession session, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Số lượng phải lớn hơn 0");
        }

        Product product = productService.getById(productId);
        if (!product.isActive()) {
            throw new BadRequestException("Sản phẩm hiện không mở bán");
        }

        CartDto cart = getCart(session);
        Optional<CartItemDto> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        int targetQuantity = quantity;
        if (existingItem.isPresent()) {
            targetQuantity += existingItem.get().getQuantity();
        }

        if (targetQuantity > product.getStockQuantity()) {
            throw new BadRequestException("Số lượng tồn kho không đủ (chỉ còn " + product.getStockQuantity() + " " + product.getUnit() + ")");
        }

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(targetQuantity);
        } else {
            CartItemDto newItem = CartItemDto.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productCode(product.getCode())
                    .imageUrl(product.getImageUrl())
                    .unit(product.getUnit())
                    .unitPrice(product.getPrice())
                    .quantity(quantity)
                    .build();
            cart.getItems().add(newItem);
        }

        recalculateCart(cart);
    }

    @Override
    public void updateQuantity(HttpSession session, Long productId, int quantity) {
        CartDto cart = getCart(session);
        if (quantity <= 0) {
            removeFromCart(session, productId);
            return;
        }

        Product product = productService.getById(productId);
        if (quantity > product.getStockQuantity()) {
            throw new BadRequestException("Số lượng tồn kho không đủ (chỉ còn " + product.getStockQuantity() + " " + product.getUnit() + ")");
        }

        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        recalculateCart(cart);
    }

    @Override
    public void removeFromCart(HttpSession session, Long productId) {
        CartDto cart = getCart(session);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        recalculateCart(cart);
    }

    @Override
    public void applyVoucher(HttpSession session, String voucherCode) {
        CartDto cart = getCart(session);
        if (voucherCode == null || voucherCode.isBlank()) {
            removeVoucher(session);
            return;
        }

        BigDecimal discount = voucherService.calculateDiscount(voucherCode.trim(), cart.getTotalAmount());
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Mã voucher không hợp lệ hoặc đơn hàng chưa đạt điều kiện tối thiểu!");
        }

        cart.setVoucherCode(voucherCode.trim().toUpperCase());
        cart.setDiscountAmount(discount);
    }

    @Override
    public void removeVoucher(HttpSession session) {
        CartDto cart = getCart(session);
        cart.setVoucherCode(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
    }

    @Override
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    private void recalculateCart(CartDto cart) {
        if (cart.getVoucherCode() != null) {
            try {
                BigDecimal discount = voucherService.calculateDiscount(cart.getVoucherCode(), cart.getTotalAmount());
                cart.setDiscountAmount(discount);
            } catch (Exception e) {
                cart.setVoucherCode(null);
                cart.setDiscountAmount(BigDecimal.ZERO);
            }
        }
    }
}

