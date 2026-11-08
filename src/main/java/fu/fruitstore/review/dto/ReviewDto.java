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

    // Tùy chọn: Lưu ID đơn hàng khi gửi đánh giá/khiếu nại từ trang Chi tiết đơn hàng
    private Long orderId;

    @NotNull(message = "Thiếu thông tin sản phẩm")
    private Long productId;

    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao")
    private int rating = 5;

    @NotBlank(message = "Vui lòng nhập nội dung đánh giá hoặc mô tả tình trạng quả hỏng")
    private String comment;

    // BỔ SUNG CHO UC-15: Đường dẫn hình ảnh báo cáo quả bị dập nát, hư hỏng
    private String imageUrl;
}