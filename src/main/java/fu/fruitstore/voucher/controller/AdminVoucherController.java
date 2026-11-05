package fu.fruitstore.voucher.controller;

import fu.fruitstore.voucher.dto.VoucherDto;
import fu.fruitstore.voucher.entity.Voucher;
import fu.fruitstore.voucher.services.VoucherService;
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
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public String listVouchers(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              Model model) {
        Page<Voucher> voucherPage = voucherService.getAllVouchers(keyword,
                PageRequest.of(page, size, Sort.by("id").descending()));
        model.addAttribute("voucherPage", voucherPage);
        model.addAttribute("keyword", keyword);
        return "admin/vouchers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("voucherDto")) {
            model.addAttribute("voucherDto", new VoucherDto());
        }
        return "admin/vouchers/form";
    }

    @PostMapping("/save")
    public String saveVoucher(@Valid @ModelAttribute("voucherDto") VoucherDto dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/vouchers/form";
        }

        try {
            if (dto.getId() == null) {
                voucherService.create(dto);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm voucher khuyến mãi thành công!");
            } else {
                voucherService.update(dto.getId(), dto);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật voucher thành công!");
            }
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/vouchers/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Voucher voucher = voucherService.getById(id);
        VoucherDto dto = new VoucherDto();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setName(voucher.getName());
        dto.setDiscountPercent(voucher.getDiscountPercent());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setMinOrderAmount(voucher.getMinOrderAmount());
        dto.setStartDate(voucher.getStartDate());
        dto.setEndDate(voucher.getEndDate());
        dto.setUsageLimit(voucher.getUsageLimit());
        dto.setUsedCount(voucher.getUsedCount());
        dto.setActive(voucher.isActive());

        model.addAttribute("voucherDto", dto);
        return "admin/vouchers/form";
    }

    @PostMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        voucherService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa voucher thành công!");
        return "redirect:/admin/vouchers";
    }
}

