package fu.fruitstore.cart.repository;

import fu.fruitstore.cart.entity.UserCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserCartRepository extends JpaRepository<UserCart, Long> {

    @Query("SELECT uc FROM UserCart uc WHERE uc.user.id = :userId")
    Optional<UserCart> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCart uc WHERE uc.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}

