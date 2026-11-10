package fu.fruitstore.auth.repository;

import fu.fruitstore.auth.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findTopByEmailAndOtpCodeAndIsUsedFalseOrderByCreatedAtDesc(String email, String otpCode);

    @Modifying
    @Query("UPDATE PasswordResetOtp o SET o.isUsed = true WHERE o.email = :email")
    void invalidateAllByEmail(@Param("email") String email);
}
