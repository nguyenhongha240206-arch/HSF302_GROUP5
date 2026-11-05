package fu.fruitstore.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LossSlipCreateDto {

    private String note;

    @NotEmpty(message = "Phiếu hao hụt phải có ít nhất 1 sản phẩm")
    @Valid
    private List<LossSlipItemDto> items = new ArrayList<>();
}

