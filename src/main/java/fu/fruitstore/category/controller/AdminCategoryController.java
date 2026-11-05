package fu.fruitstore.category.controller;

import fu.fruitstore.category.dto.CategoryDto;
import fu.fruitstore.category.entity.Category;
import fu.fruitstore.category.services.CategoryService;
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
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 Model model) {
        Page<Category> categoryPage = categoryService.getAllCategories(keyword,
                PageRequest.of(page, size, Sort.by("displayOrder").ascending()));
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("keyword", keyword);
        return "admin/categories/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("categoryDto")) {
            model.addAttribute("categoryDto", new CategoryDto());
        }
        return "admin/categories/form";
    }

    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute("categoryDto") CategoryDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/categories/form";
        }

        try {
            if (dto.getId() == null) {
                categoryService.create(dto);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục mới thành công!");
            } else {
                categoryService.update(dto.getId(), dto);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
            }
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/categories/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getById(id);
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setCode(category.getCode());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setDisplayOrder(category.getDisplayOrder());
        dto.setActive(category.isActive());

        model.addAttribute("categoryDto", dto);
        return "admin/categories/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        categoryService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục thành công!");
        return "redirect:/admin/categories";
    }
}

