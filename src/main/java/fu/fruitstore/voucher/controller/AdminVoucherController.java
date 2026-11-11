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
@PreAuthorize("hasRole('ADMIN')")
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
                             Model model,
                             RedirectAttributes redirectAttributes) {
        java.time.LocalDate today = java.time.LocalDate.now();

        // 1. Kiểm tra ngày bắt đầu: không tính các ngày trước đó, chỉ chấp nhận từ ngày hiện tại về sau
        if (dto.getStartDate() != null && dto.getStartDate().isBefore(today)) {
            bindingResult.rejectValue("startDate", "past.startDate", "Ngày bắt đầu phải từ ngày hiện tại trở đi, không tính các ngày trước đó!");
        }

        // 2. Kiểm tra ngày kết thúc sau ngày bắt đầu
        if (dto.getEndDate() != null && dto.getStartDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            bindingResult.rejectValue("endDate", "error.endDate", "Ngày kết thúc không thể trước ngày bắt đầu!");
        }

        // 3. Kiểm tra trùng mã code: báo lỗi "Mã khuyến mãi này đã tồn tại trên hệ thống."
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            if (voucherService.existsByCode(dto.getCode().trim(), dto.getId())) {
                bindingResult.rejectValue("code", "duplicate.code", "Mã khuyến mãi này đã tồn tại trên hệ thống.");
            }
        }

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
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/vouchers/form";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        voucherService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái kích hoạt voucher thành công!");
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/scan-expired")
    public String scanExpiredVouchers(RedirectAttributes redirectAttributes) {
        voucherService.deactivateExpiredVouchers();
        redirectAttributes.addFlashAttribute("successMessage", "Đã quét và đóng các voucher chạm mốc hết hạn hoặc hết lượt dùng!");
        return "redirect:/admin/vouchers";
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

