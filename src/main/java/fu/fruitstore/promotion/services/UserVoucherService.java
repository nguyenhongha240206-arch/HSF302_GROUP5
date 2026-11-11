package fu.fruitstore.promotion.services;

import fu.fruitstore.promotion.entity.UserVoucher;
import fu.fruitstore.voucher.entity.Voucher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserVoucherService {
    boolean collectVoucher(String userEmail, Long voucherId);
    List<UserVoucher> getMyActiveVouchers(String userEmail);
    List<UserVoucher> getMyAllVouchers(String userEmail);
    List<Long> getCollectedVoucherIds(String userEmail);
    Optional<Voucher> findBestVoucherForOrder(String userEmail, BigDecimal orderAmount);
    void markVoucherAsUsed(String userEmail, String voucherCode);
}

