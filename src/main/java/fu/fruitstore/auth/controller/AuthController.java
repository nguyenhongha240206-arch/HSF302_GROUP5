package fu.fruitstore.auth.controller;

import fu.fruitstore.auth.dto.ForgotPasswordDto;
import fu.fruitstore.auth.dto.RegisterDto;
import fu.fruitstore.auth.dto.ResetPasswordDto;
import fu.fruitstore.auth.dto.VerifyOtpDto;
import fu.fruitstore.auth.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Email/Số điện thoại hoặc mật khẩu không chính xác!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công!");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerDto")) {
            model.addAttribute("registerDto", new RegisterDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@Valid @ModelAttribute("registerDto") RegisterDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            bindingResult.rejectValue("email", "error.registerDto", e.getMessage());
            return "auth/register";
        }
    }

    // ==========================================
    // FORGOT / RESET PASSWORD WITH OTP (UC-03)
    // ==========================================

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        if (!model.containsAttribute("forgotPasswordDto")) {
            model.addAttribute("forgotPasswordDto", new ForgotPasswordDto());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@Valid @ModelAttribute("forgotPasswordDto") ForgotPasswordDto dto,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            userService.requestPasswordResetOtp(dto.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư!");
            return "redirect:/auth/verify-otp?email=" + dto.getEmail();
        } catch (Exception e) {
            bindingResult.rejectValue("email", "error.forgotPasswordDto", e.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(value = "email", required = false) String email, Model model) {
        VerifyOtpDto dto = new VerifyOtpDto();
        if (email != null) {
            dto.setEmail(email);
        }
        model.addAttribute("verifyOtpDto", dto);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String handleVerifyOtp(@Valid @ModelAttribute("verifyOtpDto") VerifyOtpDto dto,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/verify-otp";
        }

        boolean isValid = userService.verifyPasswordResetOtp(dto.getEmail(), dto.getOtpCode());
        if (!isValid) {
            bindingResult.rejectValue("otpCode", "error.verifyOtpDto", "Mã OTP không đúng hoặc đã hết hạn!");
            return "auth/verify-otp";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Xác thực mã OTP thành công! Vui lòng đặt mật khẩu mới.");
        return "redirect:/auth/reset-password?email=" + dto.getEmail() + "&otp=" + dto.getOtpCode();
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "email", required = false) String email,
                                    @RequestParam(value = "otp", required = false) String otp,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        if (email == null || otp == null || !userService.verifyPasswordResetOtp(email, otp)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên xác thực đã hết hạn hoặc không hợp lệ. Vui lòng thử lại!");
            return "redirect:/auth/forgot-password";
        }

        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setEmail(email);
        dto.setOtpCode(otp);
        model.addAttribute("resetPasswordDto", dto);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@Valid @ModelAttribute("resetPasswordDto") ResetPasswordDto dto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        try {
            userService.resetPasswordWithOtp(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            bindingResult.rejectValue("newPassword", "error.resetPasswordDto", e.getMessage());
            return "auth/reset-password";
        }
    }
}
