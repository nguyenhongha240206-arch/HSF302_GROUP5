package fu.fruitstore.product.controller;

import fu.fruitstore.category.services.CategoryService;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.entity.ProductType;
import fu.fruitstore.product.services.ProductService;
import fu.fruitstore.review.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class StoreProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    @GetMapping("/catalog")
    public String catalog(@RequestParam(value = "categoryId", required = false) Long categoryId,
                          @RequestParam(value = "productType", required = false) ProductType productType,
                          @RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                          @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                          @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
                          @RequestParam(value = "direction", defaultValue = "DESC") String direction,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "12") int size,
                          Model model) {

        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<Product> productsPage = productService.filterProducts(
                categoryId, productType, keyword, minPrice, maxPrice, PageRequest.of(page, size, sort));

        model.addAttribute("productsPage", productsPage);
        model.addAttribute("categories", categoryService.getAllActive());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedProductType", productType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        return "store/catalog";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.getReviewsByProduct(id));
        model.addAttribute("averageRating", reviewService.getAverageRating(id));
        model.addAttribute("totalReviews", reviewService.countReviewsByProduct(id));
        return "store/product-detail";
    }
}

