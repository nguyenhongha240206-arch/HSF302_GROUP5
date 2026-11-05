package fu.fruitstore.inventory.dto;

import fu.fruitstore.inventory.entity.LossReason;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LossSlipItemDto {

    @NotNull(message = "Vui lòng chọn sản phẩm hoa quả bị hao hụt")
    private Long productId;

    @NotNull(message = "Vui lòng nhập số lượng hao hụt")
    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    private Integer quantity;

    @NotNull(message = "Vui lòng chọn lý do hao hụt")
    private LossReason lossReason = LossReason.SPOILED;

    private String note;
}

