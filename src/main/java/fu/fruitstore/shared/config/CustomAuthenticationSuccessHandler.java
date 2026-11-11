package fu.fruitstore.shared.config;

import fu.fruitstore.cart.services.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final CartService cartService;

    public CustomAuthenticationSuccessHandler(@Lazy CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Tự động khôi phục giỏ hàng đã lưu trong database của user vào session ngay khi đăng nhập (UC-11)
        try {
            if (authentication != null && authentication.getName() != null) {
                cartService.restoreCartForUser(authentication.getName(), request.getSession());
            }
        } catch (Exception ignored) {
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isAdmin = authorities.stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN")
        );

        if (isAdmin) {
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, "/admin");
            return;
        }

        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            if (targetUrl != null && !targetUrl.contains("/auth/login") && !targetUrl.contains("/auth/register")) {
                super.onAuthenticationSuccess(request, response, authentication);
                return;
            }
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}
