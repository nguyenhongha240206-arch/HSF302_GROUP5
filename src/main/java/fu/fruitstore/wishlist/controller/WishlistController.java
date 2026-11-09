package fu.fruitstore.wishlist.controller;

import fu.fruitstore.wishlist.services.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails user, Model model) {
        model.addAttribute("wishlistItems", wishlistService.getItems(user.getUsername()));
        return "store/wishlist";
    }

    @PostMapping("/toggle/{productId}")
    public String toggle(@PathVariable Long productId,
                         @RequestParam(defaultValue = "/wishlist") String redirectUrl,
                         @AuthenticationPrincipal UserDetails user,
                         RedirectAttributes redirectAttributes) {
        wishlistService.toggle(user.getUsername(), productId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật danh sách yêu thích.");
        if (redirectUrl != null && redirectUrl.startsWith("/") && !redirectUrl.startsWith("//")) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/wishlist";
    }
}
