package fu.fruitstore.home.controller;

import fu.fruitstore.category.services.CategoryService;
import fu.fruitstore.product.services.ProductService;
import fu.fruitstore.promotion.services.BannerService;
import fu.fruitstore.voucher.services.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final VoucherService voucherService;
    private final BannerService bannerService;

    @GetMapping({"/", "/home"})
    public String index(Model model) {
        model.addAttribute("banners", bannerService.getActiveBanners());
        model.addAttribute("categories", categoryService.getAllActive());
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        model.addAttribute("newestProducts", productService.getNewestProducts());
        model.addAttribute("giftBaskets", productService.getGiftBaskets());
        model.addAttribute("activeVouchers", voucherService.getActiveVouchers());
        return "store/home";
    }

    @GetMapping("/about")
    public String about() {
        return "store/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "store/contact";
    }
}

