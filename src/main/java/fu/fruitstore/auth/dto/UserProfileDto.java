package fu.fruitstore.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDto {

    private Long id;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    private String email;
    private String phoneNumber;
    private String address;
    private Long selectedAddressId;
}
