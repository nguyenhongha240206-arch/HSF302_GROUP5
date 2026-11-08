package fu.fruitstore.product.dto;

import fu.fruitstore.product.entity.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProductDto {

    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String code;

    private String description;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán phải lớn hơn 0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn không thể âm")
    private Integer stockQuantity = 0;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unit;

    private String origin;
    private String imageUrl;

    @DecimalMin(value = "0.0", message = "Độ Brix không thể âm")
    @DecimalMax(value = "100.0", message = "Độ Brix không hợp lệ")
    private BigDecimal sweetnessBrix;

    private LocalDate receivedDate;

    @NotNull(message = "Vui lòng chọn loại sản phẩm")
    private ProductType productType = ProductType.FRUIT;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    private boolean featured = false;
    private boolean active = true;
}

