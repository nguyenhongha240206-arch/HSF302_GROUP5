package fu.fruitstore.product.repository;

import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.entity.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByCode(String code);

    @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.id = :id")
    Optional<Product> findWithCategoryById(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    boolean existsByCode(String code);
    List<Product> findByFeaturedTrueAndActiveTrue();
    List<Product> findTop8ByActiveTrueOrderByCreatedAtDesc();
    List<Product> findByProductTypeAndActiveTrue(ProductType productType);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:productType IS NULL OR p.productType = :productType) AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.origin) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:origin IS NULL OR LOWER(p.origin) = LOWER(:origin)) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> filterProducts(@Param("categoryId") Long categoryId,
                                 @Param("productType") ProductType productType,
                                 @Param("keyword") String keyword,
                                 @Param("origin") String origin,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    Page<Product> searchForAdmin(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity <= :threshold AND p.active = true")
    long countLowStockProducts(@Param("threshold") int threshold);

    List<Product> findByStockQuantityLessThanEqualAndActiveTrue(int threshold);

    @Query("SELECT DISTINCT p.origin FROM Product p WHERE p.active = true AND p.origin IS NOT NULL ORDER BY p.origin")
    List<String> findActiveOrigins();
}
