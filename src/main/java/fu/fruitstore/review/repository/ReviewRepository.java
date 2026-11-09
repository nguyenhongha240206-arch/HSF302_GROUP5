package fu.fruitstore.review.repository;

import fu.fruitstore.review.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = "user")
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    @EntityGraph(attributePaths = {"user", "product"})
    List<Review> findByUserEmailOrderByCreatedAtDesc(String email);

    long countByProductId(Long productId);

    @Query("SELECT COALESCE(AVG(r.rating), 5.0) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRating(@Param("productId") Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);
}