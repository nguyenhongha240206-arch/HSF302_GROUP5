package fu.fruitstore.inventory.repository;

import fu.fruitstore.inventory.entity.LossSlip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface LossSlipRepository extends JpaRepository<LossSlip, Long> {

    Optional<LossSlip> findBySlipCode(String slipCode);

    @Query("SELECT ls FROM LossSlip ls WHERE " +
           "(:keyword IS NULL OR LOWER(ls.slipCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ls.note) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<LossSlip> searchLossSlips(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COALESCE(SUM(ls.totalLossAmount), 0) FROM LossSlip ls")
    BigDecimal calculateTotalLossAmount();
}

