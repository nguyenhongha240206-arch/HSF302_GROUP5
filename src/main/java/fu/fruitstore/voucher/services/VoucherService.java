package fu.fruitstore.voucher.services;

import fu.fruitstore.voucher.dto.VoucherDto;
import fu.fruitstore.voucher.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface VoucherService {
    List<Voucher> getActiveVouchers();
    Page<Voucher> getAllVouchers(String keyword, Pageable pageable);
    Voucher getById(Long id);
    Voucher getByCode(String code);
    BigDecimal calculateDiscount(String code, BigDecimal orderAmount);
    Voucher create(VoucherDto dto);
    Voucher update(Long id, VoucherDto dto);
    void delete(Long id);
    void incrementUsage(String code);
}

