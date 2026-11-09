package fu.fruitstore.cart.services;

import fu.fruitstore.cart.dto.BasketPackagingStyle;
import fu.fruitstore.cart.dto.CartDto;
import fu.fruitstore.cart.dto.CartItemDto;
import fu.fruitstore.cart.dto.CustomBasketRequestDto;
import fu.fruitstore.cart.dto.CustomFruitSelectionDto;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.services.ProductService;
import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.voucher.entity.Voucher;
import fu.fruitstore.voucher.services.VoucherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        String targetItemId = "P_" + productId;

        Optional<CartItemDto> existingItem = cart.getItems().stream()
                .filter(item -> targetItemId.equals(item.getItemId()))
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
                    .itemId(targetItemId)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productCode(product.getCode())
                    .imageUrl(product.getImageUrl())
                    .unit(product.getUnit())
                    .unitPrice(product.getPrice())
                    .quantity(quantity)
                    .itemType("REGULAR")
                    .build();
            cart.getItems().add(newItem);
        }

        recalculateCart(cart);
    }

    @Override
    public void addCustomBasketToCart(HttpSession session, CustomBasketRequestDto requestDto) {
        if (requestDto.getSelectedFruits() == null || requestDto.getSelectedFruits().isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ít nhất 1 loại hoa quả cho giỏ quà của bạn!");
        }

        // Verify and populate fruit details & stock
        List<CustomFruitSelectionDto> validFruits = new ArrayList<>();
        for (CustomFruitSelectionDto fruitItem : requestDto.getSelectedFruits()) {
            if (fruitItem.getQuantity() > 0 && fruitItem.getProductId() != null) {
                Product product = productService.getById(fruitItem.getProductId());
                if (!product.isActive()) {
                    throw new BadRequestException("Sản phẩm '" + product.getName() + "' hiện không mở bán.");
                }
                if (fruitItem.getQuantity() > product.getStockQuantity()) {
                    throw new BadRequestException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho (còn: " + product.getStockQuantity() + " " + product.getUnit() + ")");
                }
                fruitItem.setProductName(product.getName());
                fruitItem.setProductCode(product.getCode());
                fruitItem.setUnitPrice(product.getPrice());
                fruitItem.setUnit(product.getUnit());
                fruitItem.setImageUrl(product.getImageUrl());
                validFruits.add(fruitItem);
            }
        }

        if (validFruits.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn số lượng lớn hơn 0 cho ít nhất 1 loại hoa quả!");
        }

        requestDto.setSelectedFruits(validFruits);
        BasketPackagingStyle style = BasketPackagingStyle.getById(requestDto.getPackagingStyleId());

        String basketItemId = "BASKET_" + UUID.randomUUID().toString().substring(0, 8);
        BigDecimal grandTotal = requestDto.calculateGrandTotal();
        String summary = requestDto.generateSummaryText();

        CartItemDto customBasketItem = CartItemDto.builder()
                .itemId(basketItemId)
                .productId(null)
                .productName(requestDto.getBasketName() != null && !requestDto.getBasketName().isBlank() 
                        ? requestDto.getBasketName() : "Giỏ Quà Hoa Quả Tự Thiết Kế")
                .productCode("CUSTOM_GIFT_" + style.getId())
                .imageUrl(style.getImageUrl())
                .unit("giỏ")
                .unitPrice(grandTotal)
                .quantity(1)
                .itemType("CUSTOM_BASKET")
                .basketStyleName(style.getName())
                .ribbonColor(requestDto.getRibbonColor())
                .greetingCardMessage(requestDto.getGreetingCardMessage())
                .customBasketSummary(summary)
                .customFruitItems(validFruits)
                .build();

        CartDto cart = getCart(session);
        cart.getItems().add(customBasketItem);
        recalculateCart(cart);
    }

    @Override
    public void updateQuantity(HttpSession session, String itemId, int quantity) {
        CartDto cart = getCart(session);
        if (quantity <= 0) {
            removeFromCart(session, itemId);
            return;
        }

        Optional<CartItemDto> itemOpt = cart.getItems().stream()
                .filter(item -> itemId.equals(item.getItemId()))
                .findFirst();

        if (itemOpt.isPresent()) {
            CartItemDto item = itemOpt.get();
            if ("REGULAR".equals(item.getItemType()) && item.getProductId() != null) {
                Product product = productService.getById(item.getProductId());
                if (quantity > product.getStockQuantity()) {
                    throw new BadRequestException("Số lượng tồn kho không đủ (chỉ còn " + product.getStockQuantity() + " " + product.getUnit() + ")");
                }
            }
            item.setQuantity(quantity);
        }

        recalculateCart(cart);
    }

    @Override
    public void removeFromCart(HttpSession session, String itemId) {
        CartDto cart = getCart(session);
        cart.getItems().removeIf(item -> itemId.equals(item.getItemId()));
        recalculateCart(cart);
    }

    @Override
    public void applyVoucher(HttpSession session, String voucherCode) {
        CartDto cart = getCart(session);
        if (voucherCode == null || voucherCode.isBlank()) {
            removeVoucher(session);
            return;
        }

        String cleanCode = voucherCode.trim().toUpperCase();
        Voucher voucher = voucherService.getByCode(cleanCode);

        LocalDate now = LocalDate.now();
        if (now.isBefore(voucher.getStartDate())) {
            throw new BadRequestException("Voucher '" + cleanCode + "' chưa đến thời gian áp dụng (bắt đầu từ: " + voucher.getStartDate() + ")");
        }
        if (now.isAfter(voucher.getEndDate())) {
            throw new BadRequestException("Voucher '" + cleanCode + "' đã hết hạn vào ngày " + voucher.getEndDate());
        }
        if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new BadRequestException("Voucher '" + cleanCode + "' đã hết lượt sử dụng trên hệ thống!");
        }

        BigDecimal totalAmount = cart.getTotalAmount();
        if (voucher.getMinOrderAmount() != null && totalAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            BigDecimal diff = voucher.getMinOrderAmount().subtract(totalAmount);
            throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu " + 
                    String.format("%,.0f", voucher.getMinOrderAmount()) + " đ. Bạn cần mua thêm " + 
                    String.format("%,.0f", diff) + " đ để dùng mã này!");
        }

        BigDecimal discount = voucher.calculateDiscount(totalAmount);
        cart.setVoucherCode(cleanCode);
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
