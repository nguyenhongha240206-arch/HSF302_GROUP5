package fu.fruitstore.product.controller;

import fu.fruitstore.category.services.CategoryService;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.entity.ProductType;
import fu.fruitstore.product.services.ProductService;
import fu.fruitstore.review.services.ReviewService;
import fu.fruitstore.wishlist.services.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class StoreProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;

    @GetMapping("/catalog")
    public String catalog(@RequestParam(value = "categoryId", required = false) Long categoryId,
                          @RequestParam(value = "productType", required = false) ProductType productType,
                          @RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(value = "origin", required = false) String origin,
                          @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                          @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                          @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                          @RequestParam(value = "direction", defaultValue = "DESC") String direction,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "12") int size,
                          @AuthenticationPrincipal UserDetails user,
                          Model model) {
        String safeSort = Set.of("createdAt", "price", "name").contains(sortBy) ? sortBy : "createdAt";
        Sort sort = "ASC".equalsIgnoreCase(direction) ? Sort.by(safeSort).ascending() : Sort.by(safeSort).descending();
        Page<Product> productsPage = productService.filterProducts(categoryId, productType, keyword, origin,
                minPrice, maxPrice, PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 100), sort));

        model.addAttribute("productsPage", productsPage);
        model.addAttribute("categories", categoryService.getAllActive());
        model.addAttribute("origins", productService.getOrigins());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedProductType", productType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("origin", origin);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", safeSort);
        model.addAttribute("direction", direction);
        model.addAttribute("wishlistedIds", user == null ? Set.of() : wishlistService.getProductIds(user.getUsername()));
        return "store/catalog";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable("id") Long id,
                                @AuthenticationPrincipal UserDetails user,
                                Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.getReviewsByProduct(id));
        model.addAttribute("averageRating", reviewService.getAverageRating(id));
        model.addAttribute("totalReviews", reviewService.countReviewsByProduct(id));
        model.addAttribute("isWishlisted", user != null && wishlistService.contains(user.getUsername(), id));
        return "store/product-detail";
    }
}
