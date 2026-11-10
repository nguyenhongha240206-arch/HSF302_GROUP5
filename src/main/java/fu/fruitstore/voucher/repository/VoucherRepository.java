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

    List<Voucher> findByActiveTrue();

    @Query("SELECT v FROM Voucher v WHERE " +
           "(:keyword IS NULL OR LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Voucher> searchVouchers(@Param("keyword") String keyword, Pageable pageable);

    List<Voucher> findByActiveTrueAndEndDateBefore(java.time.LocalDate date);
}

