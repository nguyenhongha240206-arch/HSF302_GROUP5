package fu.fruitstore.promotion.repository;

import fu.fruitstore.promotion.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    @Query("SELECT b FROM Banner b WHERE b.active = true " +
           "AND b.startDate <= :today AND b.endDate >= :today " +
           "ORDER BY b.displayOrder ASC, b.id DESC")
    List<Banner> findActiveBannersRunning(@Param("today") LocalDate today);

    @Query("SELECT b FROM Banner b WHERE b.active = true " +
           "AND b.position = :position " +
           "AND b.startDate <= :today AND b.endDate >= :today " +
           "ORDER BY b.displayOrder ASC, b.id DESC")
    List<Banner> findActiveBannersByPosition(@Param("position") String position, @Param("today") LocalDate today);

    @Query("SELECT b FROM Banner b WHERE " +
           "(:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.subtitle) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY b.displayOrder ASC, b.id DESC")
    Page<Banner> searchBanners(@Param("keyword") String keyword, Pageable pageable);

    List<Banner> findByActiveTrueAndEndDateBefore(LocalDate date);
}

