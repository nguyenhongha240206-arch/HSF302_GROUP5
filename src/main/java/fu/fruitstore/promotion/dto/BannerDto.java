package fu.fruitstore.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerDto {

    private Long id;

    @NotBlank(message = "Tiêu đề banner không được để trống")
    private String title;

    private String subtitle;

    private String imageUrl;

    private String targetUrl;

    @Builder.Default
    private String position = "HERO_SLIDER";

    @Builder.Default
    private Integer displayOrder = 0;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Builder.Default
    private boolean active = true;

    private MultipartFile imageFile;
}

