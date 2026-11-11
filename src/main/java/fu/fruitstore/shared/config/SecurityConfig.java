package fu.fruitstore.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Eagerly resolve CSRF token before view rendering commits the response
            .addFilterAfter(new OncePerRequestFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
                    if (csrfToken != null) {
                        csrfToken.getToken(); // Forces evaluation and saves session before response is committed
                    }
                    filterChain.doFilter(request, response);
                }
            }, CsrfFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Public static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**", "/favicon.ico", "/error/**").permitAll()
                // Public store pages, promotions & auth
                .requestMatchers("/", "/home", "/about", "/contact", "/catalog/**", "/products/**", "/cart/**", "/auth/**", "/custom-basket/**", "/promotions/**", "/api/vouchers/**").permitAll()
                // Admin (Chủ shop) management
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Customer actions
                .requestMatchers("/checkout/**", "/orders/**", "/profile/**", "/account/**", "/reviews/add/**", "/wishlist/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("FruitStoreUniqueAndSecretKey2026")
                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
                .rememberMeParameter("remember-me")
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }
}
