package fu.fruitstore.order.dto;

import fu.fruitstore.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutDto {

    @NotBlank(message = "Họ và tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ (10 chữ số)")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ nhận hàng chi tiết không được để trống")
    private String shippingAddress;

    // UC-11: Khung giờ giao hàng
    private String deliveryTimeType = "EXPRESS_2H"; // EXPRESS_2H (Giao gấp) / SCHEDULED (Hẹn giờ)
    private String deliveryTimeSlot = "Giao gấp trong 2 giờ"; // e.g. 08:00 - 11:30, 13:30 - 17:30, 18:00 - 21:00
    private String deliveryDate; // Ngày hẹn giao

    // UC-11: Tiêu chuẩn đóng gói & chọn hoa quả
    private String packagingRequirement = "Chọn quả tươi cứng đẹp để thắp hương / làm lễ"; 

    private String note;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod = PaymentMethod.COD;
}
