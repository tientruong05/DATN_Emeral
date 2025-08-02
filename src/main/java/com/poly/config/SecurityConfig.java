package com.poly.config;

import com.poly.entity.User;
import com.poly.service.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean; // Make sure this is imported
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Make sure this is imported
import org.springframework.security.crypto.password.PasswordEncoder; // Make sure this is imported
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // THIS IS THE MISSING PART OR INCORRECTLY PLACED PART
    @Bean
    public PasswordEncoder passwordEncoder() { // This method defines the BCryptPasswordEncoder bean
        return new BCryptPasswordEncoder();
    }

    // You can keep configureGlobal or remove it if not needed depending on your
    // Spring Security version.
    // For Spring Security 6+, the DaoAuthenticationProvider is often configured
    // directly
    // within the SecurityFilterChain or implicitly.
    // Let's ensure DaoAuthenticationProvider is correctly set up for your
    // CustomUserDetailsService.

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // This method is often replaced by directly using authenticationProvider() in
    // HttpSecurity
    // For Spring Security 6+, it's more common to configure this directly in the
    // http.build() part.
    // Let's try removing configureGlobal and rely on the new way.
    /*
     * @Autowired
     * public void configureGlobal(AuthenticationManagerBuilder auth) throws
     * Exception {
     * auth.userDetailsService(customUserDetailsService).passwordEncoder(
     * passwordEncoder());
     * }
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.poly.service.CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
            }
            response.sendRedirect("/index");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF (cân nhắc kỹ trong môi trường thực tế)
                .authorizeHttpRequests(authorize -> authorize
                        // Các tài nguyên công khai, ai cũng có thể truy cập
                        .requestMatchers("/", "/index", "/Login_Sigin", "/login", "/register",
                                "/search**", "/forgot-password", "/changePass","/Policy", "/Terms","/About",
                                "/css/**", "/js/**", "/images/**", "/course-detail/**", "/list/**", "/upload/**")
                        .permitAll()

                        // Các URL chỉ ADMIN mới được truy cập
                        .requestMatchers("/admin/**", "/Crud_Course", "/Crud_User", "/admin/discount",
                                "/Crud_Categories", "/Statistic_Revenue", "/admin/statistics/certificate",
                                "/admin/statistics/students")
                        .hasRole("ADMIN") // Yêu cầu vai trò ADMIN

                        // Các URL dành cho cả USER và ADMIN (ví dụ: hồ sơ, giỏ hàng, khóa học đang học)
                        .requestMatchers("/profile", "/change-password-authenticated", "/CourseInProgess",
                                "/history-payment", "/cart")
                        .hasAnyRole("USER", "ADMIN")

                        // Bất kỳ yêu cầu nào khác đều yêu cầu xác thực
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/Login_Sigin") // Trang đăng nhập tùy chỉnh của bạn
                        .loginProcessingUrl("/login") // URL mà form đăng nhập sẽ gửi đến
                        .usernameParameter("email")
                        .defaultSuccessUrl("/index", true) // Chuyển hướng sau khi đăng nhập thành công
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/Login_Sigin?error=true") // Chuyển hướng khi đăng nhập thất bại
                        .permitAll() // Cho phép tất cả truy cập trang đăng nhập
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL để logout
                        .logoutSuccessUrl("/index?logout") // Chuyển hướng sau khi logout thành công
                        .invalidateHttpSession(true) // Hủy phiên hiện tại
                        .deleteCookies("JSESSIONID") // Xóa cookie phiên
                        .permitAll() // Cho phép tất cả truy cập URL logout
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")); // Trang lỗi khi không có quyền truy cập

        // Add the authenticationProvider to HttpSecurity.
        // For Spring Security 6+, you often configure authentication directly in the
        // HttpSecurity chain
        // or ensure the DaoAuthenticationProvider bean is available.
        // Spring Boot usually auto-configures AuthenticationManager if an
        // AuthenticationProvider bean is present.
        // No explicit .authenticationProvider() call here is usually needed if the bean
        // is defined.

        return http.build();
    }
}