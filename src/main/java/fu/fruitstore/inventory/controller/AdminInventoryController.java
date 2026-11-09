package fu.fruitstore.inventory.controller;

import fu.fruitstore.inventory.dto.LossSlipCreateDto;
import fu.fruitstore.inventory.dto.LossSlipItemDto;
import fu.fruitstore.inventory.entity.LossReason;
import fu.fruitstore.inventory.entity.LossSlip;
import fu.fruitstore.inventory.services.InventoryService;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.services.ProductService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    @GetMapping("/slips")
    public String listLossSlips(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        Page<LossSlip> slipPage = inventoryService.getAllLossSlips(keyword,
                PageRequest.of(page, size, Sort.by("slipDate").descending()));

        model.addAttribute("slipPage", slipPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalLossAmount", inventoryService.getTotalLossAmount());
        return "admin/inventory/loss-slips";
    }

    @GetMapping("/slips/new")
    public String showCreateForm(Model model) {
        LossSlipCreateDto dto = new LossSlipCreateDto();
        dto.getItems().add(new LossSlipItemDto());

        model.addAttribute("slipDto", dto);
        model.addAttribute("products", productService.searchForAdmin(null, PageRequest.of(0, 100)).getContent());
        model.addAttribute("lossReasons", LossReason.values());
        return "admin/inventory/create-loss-slip";
    }

    @PostMapping("/slips/save")
    public String saveLossSlip(@Valid @ModelAttribute("slipDto") LossSlipCreateDto dto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.searchForAdmin(null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("lossReasons", LossReason.values());
            return "admin/inventory/create-loss-slip";
        }

        try {
            LossSlip slip = inventoryService.createLossSlip(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu hao hụt/hủy hoa quả thành công! Mã: " + slip.getSlipCode());
            return "redirect:/admin/inventory/slips";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("products", productService.searchForAdmin(null, PageRequest.of(0, 100)).getContent());
            model.addAttribute("lossReasons", LossReason.values());
            return "admin/inventory/create-loss-slip";
        }
    }

    @GetMapping("/slips/{id}")
    public String viewSlipDetail(@PathVariable("id") Long id, Model model) {
        LossSlip slip = inventoryService.getById(id);
        model.addAttribute("slip", slip);
        return "admin/inventory/slip-detail";
    }

    @PostMapping("/slips/{id}/delete")
    public String deleteSlip(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        inventoryService.deleteLossSlip(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phiếu hao hụt và hoàn trả số lượng vào kho!");
        return "redirect:/admin/inventory/slips";
    }

    @PostMapping("/stock/{id}")
    public String updateStock(@PathVariable("id") Long id,
                              @RequestParam("quantity") int quantity,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              RedirectAttributes redirectAttributes) {
        try {
            inventoryService.updateStock(id, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tồn kho thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        if (keyword != null && !keyword.isBlank()) {
            return "redirect:/admin/inventory/stock?keyword=" + java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/admin/inventory/stock";
    }

    @GetMapping("/stock")
    public String stockMonitoring(@RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "15") int size,
                                 HttpServletRequest request,
                                 Model model) {
        // Resolve the lazily-created CSRF token before Thymeleaf starts writing
        // the page, because each row below contains a POST form.
        Object csrfAttribute = request.getAttribute(CsrfToken.class.getName());
        if (csrfAttribute instanceof CsrfToken csrfToken) {
            csrfToken.getToken();
        }

        Page<Product> products = productService.searchForAdmin(keyword,
                PageRequest.of(page, size, Sort.by("stockQuantity").ascending()));
        model.addAttribute("products", products);
        model.addAttribute("lowStockCount", productService.countLowStock(10));
        model.addAttribute("keyword", keyword);
        return "admin/inventory/stock-list";
    }
}

