package fu.fruitstore.auth.controller;

import fu.fruitstore.auth.dto.AddressDto;
import fu.fruitstore.auth.dto.ChangePasswordDto;
import fu.fruitstore.auth.dto.UserProfileDto;
import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.entity.UserAddress;
import fu.fruitstore.auth.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(value = "tab", defaultValue = "general") String activeTab,
                              Model model) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        List<UserAddress> addresses = userService.getUserAddresses(user.getId());

        if (!model.containsAttribute("profileDto")) {
            UserProfileDto dto = new UserProfileDto();
            dto.setId(user.getId());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setAddress(user.getAddress());

            // Pre-select default address if available
            addresses.stream()
                    .filter(UserAddress::isDefault)
                    .findFirst()
                    .ifPresent(a -> dto.setSelectedAddressId(a.getId()));

            if (dto.getSelectedAddressId() == null && !addresses.isEmpty()) {
                dto.setSelectedAddressId(addresses.get(0).getId());
            }

            model.addAttribute("profileDto", dto);
        }

        if (!model.containsAttribute("changePasswordDto")) {
            model.addAttribute("changePasswordDto", new ChangePasswordDto());
        }

        if (!model.containsAttribute("newAddressDto")) {
            model.addAttribute("newAddressDto", new AddressDto());
        }

        model.addAttribute("addresses", addresses);
        model.addAttribute("user", user);
        model.addAttribute("activeTab", activeTab);

        return "auth/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("profileDto") UserProfileDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.profileDto", bindingResult);
            redirectAttributes.addFlashAttribute("profileDto", dto);
            return "redirect:/profile?tab=general";
        }

        try {
            userService.updateProfile(userDetails.getUsername(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile?tab=general";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @Valid @ModelAttribute("changePasswordDto") ChangePasswordDto dto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.changePasswordDto", bindingResult);
            redirectAttributes.addFlashAttribute("changePasswordDto", dto);
            return "redirect:/profile?tab=password";
        }

        try {
            userService.changePassword(userDetails.getUsername(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("changePasswordDto", dto);
        }
        return "redirect:/profile?tab=password";
    }

    @PostMapping("/addresses/add")
    public String addAddress(@AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute("newAddressDto") AddressDto dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newAddressDto", bindingResult);
            redirectAttributes.addFlashAttribute("newAddressDto", dto);
            return "redirect:/profile?tab=addresses";
        }

        try {
            User user = userService.findByEmail(userDetails.getUsername());
            userService.addAddress(user.getId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ giao hàng mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile?tab=addresses";
    }

    @PostMapping("/addresses/{id}/update")
    public String updateAddress(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable("id") Long addressId,
                                @Valid @ModelAttribute AddressDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Thông tin địa chỉ không hợp lệ!");
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/profile?tab=addresses";
        }

        try {
            User user = userService.findByEmail(userDetails.getUsername());
            userService.updateAddress(user.getId(), addressId, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật địa chỉ nhận hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile?tab=addresses";
    }

    @PostMapping("/addresses/{id}/default")
    public String setDefaultAddress(@AuthenticationPrincipal UserDetails userDetails,
                                    @PathVariable("id") Long addressId,
                                    RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            userService.setDefaultAddress(user.getId(), addressId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thiết lập địa chỉ mặc định!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile?tab=addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(@AuthenticationPrincipal UserDetails userDetails,
                                @PathVariable("id") Long addressId,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/auth/login";
        }
        try {
            User user = userService.findByEmail(userDetails.getUsername());
            userService.deleteAddress(user.getId(), addressId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile?tab=addresses";
    }
}
