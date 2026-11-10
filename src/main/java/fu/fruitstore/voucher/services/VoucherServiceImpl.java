package fu.fruitstore.voucher.services;

import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import fu.fruitstore.voucher.dto.VoucherDto;
import fu.fruitstore.voucher.entity.Voucher;
import fu.fruitstore.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> getActiveVouchers() {
        return voucherRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Voucher> getAllVouchers(String keyword, Pageable pageable) {
        return voucherRepository.searchVouchers(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Voucher getById(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher với ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Voucher getByCode(String code) {
        return voucherRepository.findByCodeIgnoreCaseAndActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại hoặc đã hết hạn!"));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(String code, BigDecimal orderAmount) {
        if (code == null || code.isBlank()) return BigDecimal.ZERO;
        Voucher voucher = voucherRepository.findByCodeIgnoreCaseAndActiveTrue(code).orElse(null);
        if (voucher == null || !voucher.isValid(orderAmount)) return BigDecimal.ZERO;
        return voucher.calculateDiscount(orderAmount);
    }

    @Override
    @Transactional
    public Voucher create(VoucherDto dto) {
        if (voucherRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new BadRequestException("Mã voucher '" + dto.getCode() + "' đã tồn tại!");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("Ngày kết thúc không thể trước ngày bắt đầu!");
        }

        Voucher voucher = Voucher.builder()
                .code(dto.getCode().toUpperCase().trim())
                .name(dto.getName())
                .discountPercent(dto.getDiscountPercent())
                .maxDiscountAmount(dto.getMaxDiscountAmount())
                .minOrderAmount(dto.getMinOrderAmount())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .usageLimit(dto.getUsageLimit())
                .usedCount(0)
                .active(dto.isActive())
                .build();

        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher update(Long id, VoucherDto dto) {
        Voucher voucher = getById(id);

        if (!voucher.getCode().equalsIgnoreCase(dto.getCode()) && voucherRepository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new BadRequestException("Mã voucher '" + dto.getCode() + "' đã tồn tại!");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("Ngày kết thúc không thể trước ngày bắt đầu!");
        }

        voucher.setCode(dto.getCode().toUpperCase().trim());
        voucher.setName(dto.getName());
        voucher.setDiscountPercent(dto.getDiscountPercent());
        voucher.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        voucher.setMinOrderAmount(dto.getMinOrderAmount());
        voucher.setStartDate(dto.getStartDate());
        voucher.setEndDate(dto.getEndDate());
        voucher.setUsageLimit(dto.getUsageLimit());
        voucher.setActive(dto.isActive());

        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Voucher voucher = getById(id);
        // Soft delete
        voucher.setDeleted(true);
        voucher.setDeletedAt(LocalDateTime.now());
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public void incrementUsage(String code) {
        voucherRepository.findByCodeIgnoreCaseAndActiveTrue(code).ifPresent(v -> {
            v.setUsedCount(v.getUsedCount() + 1);
            voucherRepository.save(v);
        });
    }
    @Override
    @Transactional
    public void decrementUsage(String code) {
        if (code == null || code.isBlank()) {
            return;
        }

        voucherRepository.findByCodeIgnoreCase(code).ifPresent(v -> {
            if (v.getUsedCount() > 0) {
                v.setUsedCount(v.getUsedCount() - 1);
                voucherRepository.save(v);
            }
        });
    }
}

