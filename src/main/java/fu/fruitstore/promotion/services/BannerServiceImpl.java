package fu.fruitstore.promotion.services;

import fu.fruitstore.promotion.dto.BannerDto;
import fu.fruitstore.promotion.entity.Banner;
import fu.fruitstore.promotion.repository.BannerRepository;
import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private static final String UPLOAD_DIR = "uploads/banners/";

    @Override
    @Transactional(readOnly = true)
    public List<Banner> getActiveBanners() {
        return bannerRepository.findActiveBannersRunning(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Banner> getActiveBannersByPosition(String position) {
        return bannerRepository.findActiveBannersByPosition(position, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Banner> getAllBanners(String keyword, Pageable pageable) {
        return bannerRepository.searchBanners(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Banner getById(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy banner với ID: " + id));
    }

    @Override
    @Transactional
    public Banner create(BannerDto dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("Ngày kết thúc không thể trước ngày bắt đầu!");
        }

        String finalImageUrl = processImageUpload(dto.getImageFile(), dto.getImageUrl());
        if (finalImageUrl == null || finalImageUrl.isBlank()) {
            throw new BadRequestException("Vui lòng tải lên ảnh banner hoặc nhập đường dẫn ảnh hợp lệ!");
        }

        Banner banner = Banner.builder()
                .title(dto.getTitle().trim())
                .subtitle(dto.getSubtitle() != null ? dto.getSubtitle().trim() : null)
                .imageUrl(finalImageUrl)
                .targetUrl(dto.getTargetUrl() != null && !dto.getTargetUrl().isBlank() ? dto.getTargetUrl().trim() : "/catalog")
                .position(dto.getPosition() != null && !dto.getPosition().isBlank() ? dto.getPosition().trim() : "HERO_SLIDER")
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .active(dto.isActive())
                .build();

        return bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public Banner update(Long id, BannerDto dto) {
        Banner banner = getById(id);

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("Ngày kết thúc không thể trước ngày bắt đầu!");
        }

        String finalImageUrl = processImageUpload(dto.getImageFile(), dto.getImageUrl());
        if (finalImageUrl != null && !finalImageUrl.isBlank()) {
            banner.setImageUrl(finalImageUrl);
        }

        banner.setTitle(dto.getTitle().trim());
        banner.setSubtitle(dto.getSubtitle() != null ? dto.getSubtitle().trim() : null);
        banner.setTargetUrl(dto.getTargetUrl() != null && !dto.getTargetUrl().isBlank() ? dto.getTargetUrl().trim() : "/catalog");
        banner.setPosition(dto.getPosition() != null && !dto.getPosition().isBlank() ? dto.getPosition().trim() : "HERO_SLIDER");
        banner.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        banner.setStartDate(dto.getStartDate());
        banner.setEndDate(dto.getEndDate());
        banner.setActive(dto.isActive());

        return bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        Banner banner = getById(id);
        banner.setActive(!banner.isActive());
        bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Banner banner = getById(id);
        banner.setDeleted(true);
        banner.setDeletedAt(LocalDateTime.now());
        bannerRepository.save(banner);
    }

    @Override
    @Transactional
    public void deactivateExpiredBanners() {
        LocalDate today = LocalDate.now();
        List<Banner> expiredBanners = bannerRepository.findByActiveTrueAndEndDateBefore(today);
        if (!expiredBanners.isEmpty()) {
            for (Banner b : expiredBanners) {
                b.setActive(false);
            }
            bannerRepository.saveAll(expiredBanners);
            log.info("PromotionScheduler: Đã tự động tắt {} banner hết hạn vào ngày {}", expiredBanners.size(), today);
        }
    }

    private String processImageUpload(MultipartFile file, String fallbackUrl) {
        if (file != null && !file.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String originalFileName = file.getOriginalFilename();
                String ext = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    ext = originalFileName.substring(originalFileName.lastIndexOf("."));
                }
                String newFileName = UUID.randomUUID().toString() + ext;
                Path filePath = uploadPath.resolve(newFileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                return "/" + UPLOAD_DIR + newFileName;
            } catch (IOException e) {
                log.error("Lỗi khi lưu ảnh upload banner: {}", e.getMessage());
                throw new BadRequestException("Không thể lưu ảnh tải lên: " + e.getMessage());
            }
        }
        return fallbackUrl != null ? fallbackUrl.trim() : null;
    }
}

