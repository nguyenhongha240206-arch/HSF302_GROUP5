package fu.fruitstore.promotion.scheduler;

import fu.fruitstore.promotion.services.BannerService;
import fu.fruitstore.voucher.services.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionScheduler {

    private final VoucherService voucherService;
    private final BannerService bannerService;

    /**
     * Tự động quét và đóng các mã voucher đã hết hạn (endDate chạm mốc hiện tại) hoặc hết lượt (UC-13)
     * và tự động tắt các banner sự kiện khuyến mãi đã kết thúc thời gian chạy (UC-14).
     * Chạy định kỳ mỗi phút một lần.
     */
    @Scheduled(fixedRate = 60000)
    public void scanAndCloseExpiredPromotions() {
        try {
            voucherService.deactivateExpiredVouchers();
            bannerService.deactivateExpiredBanners();
        } catch (Exception e) {
            log.error("Lỗi trong quá trình quét tự động voucher/banner hết hạn: {}", e.getMessage());
        }
    }
}

