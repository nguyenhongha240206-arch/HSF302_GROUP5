package fu.fruitstore.wishlist.repository;

import fu.fruitstore.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    @Query("SELECT w FROM WishlistItem w JOIN FETCH w.product WHERE w.user.email = :email ORDER BY w.createdAt DESC")
    List<WishlistItem> findItemsByEmail(@Param("email") String email);

    Optional<WishlistItem> findByUser_EmailAndProduct_Id(String email, Long productId);
    boolean existsByUser_EmailAndProduct_Id(String email, Long productId);
}
