package fu.fruitstore.cart.controller;

import fu.fruitstore.cart.dto.BasketPackagingStyle;
import fu.fruitstore.cart.dto.CustomBasketRequestDto;
import fu.fruitstore.cart.dto.CustomFruitSelectionDto;
import fu.fruitstore.cart.services.CartService;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.entity.ProductType;
import fu.fruitstore.product.services.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/custom-basket")
@RequiredArgsConstructor
public class CustomBasketController {

    private final ProductService productService;
    private final CartService cartService;

    @GetMapping
    public String customBasketDesigner(Model model) {
        List<BasketPackagingStyle> styles = BasketPackagingStyle.getDefaultStyles();
        List<Product> availableFruits = productService.getFeaturedProducts();
        if (availableFruits.isEmpty()) {
            availableFruits = productService.getNewestProducts();
        }

        List<String> ribbonColors = Arrays.asList(
                "Đỏ Nhung May Mắn",
                "Vàng Kim Hoàng Gia",
                "Xanh Ngọc Bích Sang Trọng",
                "Hồng Pastel Thanh Lịch",
                "Tím Thủy Chung Quý Phái"
        );

        List<String> cardTemplates = Arrays.asList(
                "Kính Chúc Mừng Sinh Nhật Vui Vẻ, Hạnh Phúc & Thành Công!",
                "Kính Chúc Khai Trương Hồng Phát, Tài Lộc Như Ý!",
                "Kính Chúc Sức Khỏe Dồi Dào, Vạn Sự Bình An!",
                "Kính Tặng Quý Đối Tác - Hợp Tác Thịnh Vượng!",
                "Kính Chúc Mừng Năm Mới - An Khang Thịnh Vượng!"
        );

        CustomBasketRequestDto customBasketDto = new CustomBasketRequestDto();
        customBasketDto.setPackagingStyleId(styles.get(0).getId());

        model.addAttribute("styles", styles);
        model.addAttribute("fruits", availableFruits);
        model.addAttribute("ribbonColors", ribbonColors);
        model.addAttribute("cardTemplates", cardTemplates);
        model.addAttribute("customBasketDto", customBasketDto);

        return "store/custom-basket";
    }

    @PostMapping("/add-to-cart")
    public String addCustomBasketToCart(@ModelAttribute("customBasketDto") CustomBasketRequestDto customBasketDto,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        try {
            cartService.addCustomBasketToCart(session, customBasketDto);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã tạo và thêm giỏ quà '" + customBasketDto.getBasketName() + "' vào giỏ hàng thành công!");
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/custom-basket";
        }
    }
}

