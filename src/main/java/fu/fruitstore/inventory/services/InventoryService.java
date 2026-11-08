package fu.fruitstore.inventory.services;

import fu.fruitstore.inventory.dto.LossSlipCreateDto;
import fu.fruitstore.inventory.entity.LossSlip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface InventoryService {
    LossSlip createLossSlip(LossSlipCreateDto dto);
    Page<LossSlip> getAllLossSlips(String keyword, Pageable pageable);
    LossSlip getById(Long id);
    void deleteLossSlip(Long id);
    BigDecimal getTotalLossAmount();
    void updateStock(Long productId, int quantity);
}

