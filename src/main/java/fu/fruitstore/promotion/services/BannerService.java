package fu.fruitstore.promotion.services;

import fu.fruitstore.promotion.dto.BannerDto;
import fu.fruitstore.promotion.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BannerService {
    List<Banner> getActiveBanners();
    List<Banner> getActiveBannersByPosition(String position);
    Page<Banner> getAllBanners(String keyword, Pageable pageable);
    Banner getById(Long id);
    Banner create(BannerDto dto);
    Banner update(Long id, BannerDto dto);
    void toggleActive(Long id);
    void delete(Long id);
    void deactivateExpiredBanners();
}

