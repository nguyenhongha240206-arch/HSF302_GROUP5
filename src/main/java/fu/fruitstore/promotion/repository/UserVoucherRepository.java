package fu.fruitstore.promotion.repository;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.promotion.entity.UserVoucher;
import fu.fruitstore.voucher.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    Optional<UserVoucher> findByUserAndVoucher(User user, Voucher voucher);

    boolean existsByUserIdAndVoucherId(Long userId, Long voucherId);

    @Query("SELECT uv FROM UserVoucher uv JOIN FETCH uv.voucher v " +
           "WHERE uv.user.id = :userId AND uv.isUsed = false " +
           "ORDER BY uv.collectedAt DESC")
    List<UserVoucher> findActiveVouchersByUserId(@Param("userId") Long userId);

    @Query("SELECT uv FROM UserVoucher uv JOIN FETCH uv.voucher v " +
           "WHERE uv.user.id = :userId " +
           "ORDER BY uv.collectedAt DESC")
    List<UserVoucher> findAllByUserIdWithVoucher(@Param("userId") Long userId);

    @Query("SELECT uv.voucher.id FROM UserVoucher uv WHERE uv.user.id = :userId")
    List<Long> findCollectedVoucherIdsByUserId(@Param("userId") Long userId);

    Optional<UserVoucher> findByUserAndVoucher_CodeIgnoreCase(User user, String code);
}

