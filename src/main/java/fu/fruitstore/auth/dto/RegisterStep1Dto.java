package fu.fruitstore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterStep1Dto {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng (VD: example@gmail.com)")
    private String email;

    @Pattern(regexp = "^$|^(0[35789])[0-9]{8}$", message = "Số điện thoại không đúng định dạng (VD: 0912345678)")
    private String phoneNumber;
}
