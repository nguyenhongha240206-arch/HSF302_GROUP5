package fu.fruitstore.promotion.controller;

import fu.fruitstore.promotion.dto.BannerDto;
import fu.fruitstore.promotion.entity.Banner;
import fu.fruitstore.promotion.services.BannerService;
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

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/banners")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BannerService bannerService;

    @GetMapping
    public String listBanners(@RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              Model model) {
        Page<Banner> bannerPage = bannerService.getAllBanners(keyword,
                PageRequest.of(page, size, Sort.by("displayOrder").ascending().and(Sort.by("id").descending())));

        model.addAttribute("bannerPage", bannerPage);
        model.addAttribute("keyword", keyword);
        return "admin/banners/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("bannerDto")) {
            BannerDto dto = new BannerDto();
            dto.setStartDate(LocalDate.now());
            dto.setEndDate(LocalDate.now().plusMonths(1));
            dto.setActive(true);
            dto.setDisplayOrder(1);
            dto.setPosition("HERO_SLIDER");
            dto.setTargetUrl("/catalog");
            model.addAttribute("bannerDto", dto);
        }
        return "admin/banners/form";
    }

    @PostMapping("/save")
    public String saveBanner(@Valid @ModelAttribute("bannerDto") BannerDto dto,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (dto.getEndDate() != null && dto.getStartDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            bindingResult.rejectValue("endDate", "error.endDate", "Ngày kết thúc không thể trước ngày bắt đầu!");
        }

        if (bindingResult.hasErrors()) {
            return "admin/banners/form";
        }

        try {
            if (dto.getId() == null) {
                bannerService.create(dto);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm mới banner khuyến mãi thành công!");
            } else {
                bannerService.update(dto.getId(), dto);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin banner thành công!");
            }
            return "redirect:/admin/banners";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/banners/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Banner banner = bannerService.getById(id);
        BannerDto dto = BannerDto.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .imageUrl(banner.getImageUrl())
                .targetUrl(banner.getTargetUrl())
                .position(banner.getPosition())
                .displayOrder(banner.getDisplayOrder())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .active(banner.isActive())
                .build();

        model.addAttribute("bannerDto", dto);
        return "admin/banners/form";
    }

    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bannerService.toggleActive(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái hiển thị banner thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/banners";
    }

    @PostMapping("/{id}/delete")
    public String deleteBanner(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bannerService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa banner thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/banners";
    }
}

