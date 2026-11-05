package fu.fruitstore.inventory.services;

import fu.fruitstore.inventory.dto.LossSlipCreateDto;
import fu.fruitstore.inventory.dto.LossSlipItemDto;
import fu.fruitstore.inventory.entity.LossSlip;
import fu.fruitstore.inventory.entity.LossSlipDetail;
import fu.fruitstore.inventory.repository.LossSlipRepository;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.services.ProductService;
import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final LossSlipRepository lossSlipRepository;
    private final ProductService productService;

    @Override
    @Transactional
    public LossSlip createLossSlip(LossSlipCreateDto dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("Phiếu hao hụt phải có ít nhất 1 sản phẩm!");
        }

        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = 1000 + new Random().nextInt(9000);
        String slipCode = "LS-" + datePart + "-" + randomPart;

        LossSlip lossSlip = LossSlip.builder()
                .slipCode(slipCode)
                .slipDate(LocalDateTime.now())
                .note(dto.getNote())
                .totalLossAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (LossSlipItemDto item : dto.getItems()) {
            Product product = productService.getById(item.getProductId());

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Số lượng hao hụt của '" + product.getName() +
                        "' vượt quá tồn kho hiện tại (còn: " + product.getStockQuantity() + " " + product.getUnit() + ")");
            }

            BigDecimal costPrice = product.getPrice(); // Or base cost
            BigDecimal subTotal = costPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subTotal);

            LossSlipDetail detail = LossSlipDetail.builder()
                    .product(product)
                    .quantity(item.getQuantity())
                    .costPrice(costPrice)
                    .lossReason(item.getLossReason())
                    .note(item.getNote())
                    .build();

            lossSlip.addDetail(detail);

            // Deduct stock for lost/spoiled fruits
            productService.adjustStock(product.getId(), -item.getQuantity());
        }

        lossSlip.setTotalLossAmount(totalAmount);
        return lossSlipRepository.save(lossSlip);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LossSlip> getAllLossSlips(String keyword, Pageable pageable) {
        return lossSlipRepository.searchLossSlips(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public LossSlip getById(Long id) {
        return lossSlipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu hao hụt với ID: " + id));
    }

    @Override
    @Transactional
    public void deleteLossSlip(Long id) {
        LossSlip lossSlip = getById(id);

        // Restore inventory when loss slip is cancelled/deleted
        for (LossSlipDetail detail : lossSlip.getDetails()) {
            productService.adjustStock(detail.getProduct().getId(), detail.getQuantity());
        }

        // Soft delete
        lossSlip.setDeleted(true);
        lossSlip.setDeletedAt(LocalDateTime.now());
        lossSlipRepository.save(lossSlip);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalLossAmount() {
        return lossSlipRepository.calculateTotalLossAmount();
    }
}

