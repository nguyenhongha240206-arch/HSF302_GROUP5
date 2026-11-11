package fu.fruitstore.auth.controller;

import fu.fruitstore.auth.dto.*;
import fu.fruitstore.auth.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private String redirectIfAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            return isAdmin ? "redirect:/admin" : "redirect:/";
        }
        return null;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        String redirect = redirectIfAuthenticated();
        if (redirect != null) {
            return redirect;
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Email/Số điện thoại hoặc mật khẩu không chính xác!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công!");
        }
        return "auth/login";
    }

    // ==========================================
    // STEP 1: REGISTER - ENTER BASIC INFO & REQUEST OTP
    // ==========================================

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        String redirect = redirectIfAuthenticated();
        if (redirect != null) {
            return redirect;
        }

        if (!model.containsAttribute("registerStep1Dto")) {
            RegisterStep1Dto dto = new RegisterStep1Dto();
            String savedEmail = (String) session.getAttribute("REG_EMAIL");
            String savedName = (String) session.getAttribute("REG_FULL_NAME");
            String savedPhone = (String) session.getAttribute("REG_PHONE");
            if (savedEmail != null) dto.setEmail(savedEmail);
            if (savedName != null) dto.setFullName(savedName);
            if (savedPhone != null) dto.setPhoneNumber(savedPhone);
            model.addAttribute("registerStep1Dto", dto);
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegisterStep1(@Valid @ModelAttribute("registerStep1Dto") RegisterStep1Dto dto,
                                      BindingResult bindingResult,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.sendRegistrationOtp(dto);

            session.setAttribute("REG_FULL_NAME", dto.getFullName().trim());
            session.setAttribute("REG_EMAIL", dto.getEmail().toLowerCase().trim());
            session.setAttribute("REG_PHONE", dto.getPhoneNumber() != null ? dto.getPhoneNumber().trim() : "");
            session.setAttribute("REG_OTP_VERIFIED", false);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Mã OTP gồm 6 chữ số đã được gửi đến email " + dto.getEmail() + ". Vui lòng kiểm tra hộp thư!");
            return "redirect:/auth/register/verify-otp";
        } catch (Exception e) {
            bindingResult.rejectValue("email", "error.registerStep1Dto", e.getMessage());
            return "auth/register";
        }
    }

    // ==========================================
    // STEP 2: REGISTER - VERIFY EMAIL OTP
    // ==========================================

    @GetMapping("/register/verify-otp")
    public String registerVerifyOtpPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("REG_EMAIL");
        if (email == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập thông tin đăng ký trước!");
            return "redirect:/auth/register";
        }

        if (!model.containsAttribute("verifyOtpDto")) {
            VerifyOtpDto dto = new VerifyOtpDto();
            dto.setEmail(email);
            model.addAttribute("verifyOtpDto", dto);
        }
        model.addAttribute("regEmail", email);
        return "auth/register-verify-otp";
    }

    @PostMapping("/register/verify-otp")
    public String handleRegisterVerifyOtp(@Valid @ModelAttribute("verifyOtpDto") VerifyOtpDto dto,
                                          BindingResult bindingResult,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes,
                                          Model model) {
        String email = (String) session.getAttribute("REG_EMAIL");
        if (email == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng ký đã hết hạn. Vui lòng thử lại!");
            return "redirect:/auth/register";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("regEmail", email);
            return "auth/register-verify-otp";
        }

        boolean isValid = userService.verifyRegistrationOtp(email, dto.getOtpCode());
        if (!isValid) {
            bindingResult.rejectValue("otpCode", "error.verifyOtpDto", "Mã OTP không đúng hoặc đã hết hạn (quá 5 phút)!");
            model.addAttribute("regEmail", email);
            return "auth/register-verify-otp";
        }

        session.setAttribute("REG_OTP_VERIFIED", true);
        redirectAttributes.addFlashAttribute("successMessage", "Xác thực email thành công! Bây giờ hãy thiết lập mật khẩu cho tài khoản.");
        return "redirect:/auth/register/set-password";
    }

    @PostMapping("/register/resend-otp")
    public String handleRegisterResendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("REG_EMAIL");
        String fullName = (String) session.getAttribute("REG_FULL_NAME");
        String phone = (String) session.getAttribute("REG_PHONE");

        if (email == null || fullName == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại!");
            return "redirect:/auth/register";
        }

        try {
            RegisterStep1Dto dto = new RegisterStep1Dto();
            dto.setEmail(email);
            dto.setFullName(fullName);
            dto.setPhoneNumber(phone);
            userService.sendRegistrationOtp(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP mới đã được gửi lại vào email của bạn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/auth/register/verify-otp";
    }

    // ==========================================
    // STEP 3: REGISTER - SET PASSWORD & COMPLETE
    // ==========================================

    @GetMapping("/register/set-password")
    public String registerSetPasswordPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Boolean verified = (Boolean) session.getAttribute("REG_OTP_VERIFIED");
        String email = (String) session.getAttribute("REG_EMAIL");

        if (verified == null || !verified || email == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng xác thực mã OTP trước khi thiết lập mật khẩu!");
            return "redirect:/auth/register";
        }

        if (!model.containsAttribute("registerPasswordDto")) {
            model.addAttribute("registerPasswordDto", new RegisterPasswordDto());
        }
        model.addAttribute("regEmail", email);
        model.addAttribute("regFullName", session.getAttribute("REG_FULL_NAME"));
        return "auth/register-set-password";
    }

    @PostMapping("/register/set-password")
    public String handleRegisterSetPassword(@Valid @ModelAttribute("registerPasswordDto") RegisterPasswordDto dto,
                                            BindingResult bindingResult,
                                            HttpSession session,
                                            RedirectAttributes redirectAttributes,
                                            Model model) {
        Boolean verified = (Boolean) session.getAttribute("REG_OTP_VERIFIED");
        String email = (String) session.getAttribute("REG_EMAIL");
        String fullName = (String) session.getAttribute("REG_FULL_NAME");
        String phone = (String) session.getAttribute("REG_PHONE");

        if (verified == null || !verified || email == null || fullName == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng ký không hợp lệ. Vui lòng thử lại!");
            return "redirect:/auth/register";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("regEmail", email);
            model.addAttribute("regFullName", fullName);
            return "auth/register-set-password";
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerPasswordDto", "Mật khẩu xác nhận không khớp!");
            model.addAttribute("regEmail", email);
            model.addAttribute("regFullName", fullName);
            return "auth/register-set-password";
        }

        try {
            userService.completeRegistration(fullName, email, phone, dto);

            // Xóa session đăng ký
            session.removeAttribute("REG_FULL_NAME");
            session.removeAttribute("REG_EMAIL");
            session.removeAttribute("REG_PHONE");
            session.removeAttribute("REG_OTP_VERIFIED");

            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập với mật khẩu vừa tạo.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("regEmail", email);
            model.addAttribute("regFullName", fullName);
            return "auth/register-set-password";
        }
    }

    // ==========================================
    // FORGOT / RESET PASSWORD WITH OTP (UC-03)
    // ==========================================

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        String redirect = redirectIfAuthenticated();
        if (redirect != null) {
            return redirect;
        }

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
