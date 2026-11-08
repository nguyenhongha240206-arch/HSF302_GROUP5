package fu.fruitstore.product.controller;

import fu.fruitstore.category.services.CategoryService;
import fu.fruitstore.product.dto.ProductDto;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.entity.ProductType;
import fu.fruitstore.product.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        Page<Product> productPage = productService.searchForAdmin(keyword,
                PageRequest.of(page, size, Sort.by("id").descending()));

        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("lowStockCount", productService.countLowStock(10));
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("productDto")) {
            model.addAttribute("productDto", new ProductDto());
        }
        model.addAttribute("categories", categoryService.getAllActive());
        model.addAttribute("productTypes", ProductType.values());
        return "admin/products/form";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("productDto") ProductDto dto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllActive());
            model.addAttribute("productTypes", ProductType.values());
            return "admin/products/form";
        }

        try {
            if (dto.getId() == null) {
                productService.create(dto);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm mới thành công!");
            } else {
                productService.update(dto.getId(), dto);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin sản phẩm thành công!");
            }
            return "redirect:/admin/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.getAllActive());
            model.addAttribute("productTypes", ProductType.values());
            return "admin/products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.getById(id);
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setCode(product.getCode());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setUnit(product.getUnit());
        dto.setOrigin(product.getOrigin());
        dto.setImageUrl(product.getImageUrl());
        dto.setSweetnessBrix(product.getSweetnessBrix());
        dto.setReceivedDate(product.getReceivedDate());
        dto.setProductType(product.getProductType());
        dto.setCategoryId(product.getCategory().getId());
        dto.setFeatured(product.isFeatured());
        dto.setActive(product.isActive());

        model.addAttribute("productDto", dto);
        model.addAttribute("categories", categoryService.getAllActive());
        model.addAttribute("productTypes", ProductType.values());
        return "admin/products/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công (Soft Delete)!");
        return "redirect:/admin/products";
    }
}

