package fu.fruitstore.voucher.repository;

import fu.fruitstore.voucher.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCodeIgnoreCaseAndActiveTrue(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Voucher> findByActiveTrue();

    @Query("SELECT v FROM Voucher v WHERE " +
           "(:keyword IS NULL OR LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Voucher> searchVouchers(@Param("keyword") String keyword, Pageable pageable);

    List<Voucher> findByActiveTrueAndEndDateBefore(java.time.LocalDate date);

    @Query("SELECT v FROM Voucher v WHERE v.active = true AND (v.endDate <= :today OR v.usedCount >= v.usageLimit)")
    List<Voucher> findExpiredOrMaxedVouchers(@Param("today") java.time.LocalDate today);
}
