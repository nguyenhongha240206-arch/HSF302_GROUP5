package fu.fruitstore.promotion.services;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.repository.UserRepository;
import fu.fruitstore.promotion.entity.UserVoucher;
import fu.fruitstore.promotion.repository.UserVoucherRepository;
import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import fu.fruitstore.voucher.entity.Voucher;
import fu.fruitstore.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserVoucherServiceImpl implements UserVoucherService {

    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;

    @Override
    @Transactional
    public boolean collectVoucher(String userEmail, Long voucherId) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new BadRequestException("Vui lòng đăng nhập để thu thập voucher vào Ví cá nhân!");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản người dùng!"));

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher không tồn tại trên hệ thống!"));

        if (!voucher.isActive()) {
            throw new BadRequestException("Mã khuyến mãi này hiện đang tạm dừng!");
        }

        LocalDate today = LocalDate.now();
        if (today.isAfter(voucher.getEndDate())) {
            throw new BadRequestException("Mã khuyến mãi này đã hết hạn sử dụng!");
        }
        if (today.isBefore(voucher.getStartDate())) {
            throw new BadRequestException("Mã khuyến mãi chưa bắt đầu (bắt đầu từ " + voucher.getStartDate() + ")!");
        }
        if (voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new BadRequestException("Rất tiếc! Mã khuyến mãi này đã hết lượt thu thập trên toàn hệ thống.");
        }

        if (userVoucherRepository.existsByUserIdAndVoucherId(user.getId(), voucher.getId())) {
            throw new BadRequestException("Bạn đã thu thập voucher này vào Ví rồi!");
        }

        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .collectedAt(LocalDateTime.now())
                .isUsed(false)
                .build();

        userVoucherRepository.save(userVoucher);
        log.info("Khách hàng {} đã thu thập voucher '{}' vào Ví cá nhân.", userEmail, voucher.getCode());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVoucher> getMyActiveVouchers(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) return Collections.emptyList();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return Collections.emptyList();
        return userVoucherRepository.findActiveVouchersByUserId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVoucher> getMyAllVouchers(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) return Collections.emptyList();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return Collections.emptyList();
        return userVoucherRepository.findAllByUserIdWithVoucher(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getCollectedVoucherIds(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) return Collections.emptyList();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return Collections.emptyList();
        return userVoucherRepository.findCollectedVoucherIdsByUserId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Voucher> findBestVoucherForOrder(String userEmail, BigDecimal orderAmount) {
        if (userEmail == null || userEmail.isBlank() || orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        List<UserVoucher> myActiveVouchers = getMyActiveVouchers(userEmail);
        Voucher bestVoucher = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (UserVoucher uv : myActiveVouchers) {
            Voucher v = uv.getVoucher();
            if (v != null && v.isValid(orderAmount)) {
                BigDecimal discount = v.calculateDiscount(orderAmount);
                if (discount.compareTo(maxDiscount) > 0) {
                    maxDiscount = discount;
                    bestVoucher = v;
                }
            }
        }

        return Optional.ofNullable(bestVoucher);
    }

    @Override
    @Transactional
    public void markVoucherAsUsed(String userEmail, String voucherCode) {
        if (userEmail == null || voucherCode == null) return;
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) return;

        userVoucherRepository.findByUserAndVoucher_CodeIgnoreCase(user, voucherCode.trim())
                .ifPresent(uv -> {
                    uv.setUsed(true);
                    uv.setUsedAt(LocalDateTime.now());
                    userVoucherRepository.save(uv);
                    log.info("Đã đánh dấu voucher '{}' trong Ví của user {} là ĐÃ SỬ DỤNG.", voucherCode, userEmail);
                });
    }
}

