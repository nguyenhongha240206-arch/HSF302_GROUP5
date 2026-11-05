package fu.fruitstore.voucher.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VoucherDto {

    private Long id;

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 50, message = "Mã voucher tối đa 50 ký tự")
    private String code;

    @NotBlank(message = "Tên chương trình không được để trống")
    private String name;

    @NotNull(message = "Phần trăm giảm giá không được để trống")
    @Min(value = 1, message = "Tối thiểu giảm 1%")
    @Max(value = 100, message = "Tối đa giảm 100%")
    private Integer discountPercent;

    private BigDecimal maxDiscountAmount;
    private BigDecimal minOrderAmount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @NotNull(message = "Giới hạn lượt dùng không được để trống")
    @Min(value = 1, message = "Tối thiểu 1 lượt dùng")
    private Integer usageLimit = 100;

    private Integer usedCount = 0;
    private boolean active = true;
}

