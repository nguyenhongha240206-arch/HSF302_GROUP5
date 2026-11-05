package fu.fruitstore.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {

    @NotNull(message = "Thiếu thông tin sản phẩm")
    private Long productId;

    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao")
    private int rating = 5;

    @NotBlank(message = "Vui lòng nhập nội dung đánh giá của bạn")
    private String comment;
}

