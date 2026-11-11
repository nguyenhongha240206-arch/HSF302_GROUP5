package fu.fruitstore.promotion.controller;

import fu.fruitstore.promotion.entity.Banner;
import fu.fruitstore.promotion.entity.UserVoucher;
import fu.fruitstore.promotion.services.BannerService;
import fu.fruitstore.promotion.services.UserVoucherService;
import fu.fruitstore.voucher.entity.Voucher;
import fu.fruitstore.voucher.services.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PromotionController {

    private final BannerService bannerService;
    private final VoucherService voucherService;
    private final UserVoucherService userVoucherService;

    /**
     * UC-15: Trang Tin Ưu Đãi & Khuyến Mãi
     */
    @GetMapping("/promotions")
    public String viewPromotions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Banner> activeBanners = bannerService.getActiveBanners();
        List<Voucher> activeVouchers = voucherService.getActiveVouchers();

        List<Long> collectedVoucherIds = Collections.emptyList();
        if (userDetails != null) {
            collectedVoucherIds = userVoucherService.getCollectedVoucherIds(userDetails.getUsername());
        }

        model.addAttribute("banners", activeBanners);
        model.addAttribute("vouchers", activeVouchers);
        model.addAttribute("collectedVoucherIds", collectedVoucherIds);
        return "store/promotions";
    }

    /**
     * UC-15: Thu thập voucher vào ví cá nhân qua AJAX (không reload trang)
     */
    @PostMapping("/api/vouchers/{id}/collect")
    @ResponseBody
    public ResponseEntity<?> collectVoucherAjax(@PathVariable("id") Long voucherId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "needLogin", true,
                    "message", "Vui lòng đăng nhập để thu thập mã ưu đãi này vào Ví của bạn!"
            ));
        }

        try {
            userVoucherService.collectVoucher(userDetails.getUsername(), voucherId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã thu thập voucher thành công! Voucher đã sẵn sàng trong Ví của bạn."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * UC-15: Trang Ví Voucher Cá Nhân của Khách Hàng
     */
    @GetMapping("/account/vouchers")
    @PreAuthorize("isAuthenticated()")
    public String myVouchers(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        List<UserVoucher> allVouchers = userVoucherService.getMyAllVouchers(email);
        java.time.LocalDate today = java.time.LocalDate.now();

        // 1. Nhánh: Sẵn sàng sử dụng (Chưa dùng, còn hạn, còn lượt và đang hoạt động)
        List<UserVoucher> availableVouchers = allVouchers.stream()
                .filter(uv -> !uv.isUsed() 
                        && uv.getVoucher().isActive() 
                        && !today.isAfter(uv.getVoucher().getEndDate())
                        && uv.getVoucher().getUsedCount() < uv.getVoucher().getUsageLimit())
                .collect(Collectors.toList());

        // 2. Nhánh: Đã sử dụng (Đã áp dụng vào đơn hàng thành công)
        List<UserVoucher> usedVouchers = allVouchers.stream()
                .filter(UserVoucher::isUsed)
                .collect(Collectors.toList());

        // 3. Nhánh: Đã hết hạn (Chưa dùng nhưng đã quá hạn hoặc hết lượt hệ thống)
        List<UserVoucher> expiredVouchers = allVouchers.stream()
                .filter(uv -> !uv.isUsed() 
                        && (today.isAfter(uv.getVoucher().getEndDate()) 
                            || !uv.getVoucher().isActive() 
                            || uv.getVoucher().getUsedCount() >= uv.getVoucher().getUsageLimit()))
                .collect(Collectors.toList());

        model.addAttribute("availableVouchers", availableVouchers);
        model.addAttribute("usedVouchers", usedVouchers);
        model.addAttribute("expiredVouchers", expiredVouchers);
        return "store/my-vouchers";
    }

    /**
     * UC-15 & UC-12: API gợi ý mã voucher tối ưu nhất cho giá trị giỏ hàng
     */
    @GetMapping("/api/vouchers/best")
    @ResponseBody
    public ResponseEntity<?> getBestVoucher(@RequestParam("orderAmount") BigDecimal orderAmount,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.ok(Map.of("hasBest", false));
        }

        Optional<Voucher> bestVoucher = userVoucherService.findBestVoucherForOrder(userDetails.getUsername(), orderAmount);
        if (bestVoucher.isPresent()) {
            Voucher v = bestVoucher.get();
            BigDecimal discount = v.calculateDiscount(orderAmount);
            return ResponseEntity.ok(Map.of(
                    "hasBest", true,
                    "code", v.getCode(),
                    "name", v.getName(),
                    "discountPercent", v.getDiscountPercent(),
                    "discountAmount", discount
            ));
        }

        return ResponseEntity.ok(Map.of("hasBest", false));
    }
}

