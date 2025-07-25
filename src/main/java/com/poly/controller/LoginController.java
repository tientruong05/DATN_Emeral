package com.poly.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder; // Import Spring Security's PasswordEncoder
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication; // Thêm dòng này
import org.springframework.security.core.context.SecurityContextHolder; // Thêm dòng này
import org.springframework.security.core.userdetails.UserDetails; // Thêm dòng này (nếu bạn dùng UserDetails)

import com.poly.entity.User;
import com.poly.service.UserService;
import com.poly.service.CartService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolationException;

@Controller
public class LoginController {

    @Autowired
    UserService usrSer;

    @Autowired
    CartService cartService;

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject the PasswordEncoder bean

    // Temporary storage for reset codes
    private final ConcurrentHashMap<String, ResetToken> resetTokens = new ConcurrentHashMap<>();

    private static class ResetToken {
        String code;
        LocalDateTime expiry;
        String email;

        ResetToken(String code, LocalDateTime expiry, String email) {
            this.code = code;
            this.expiry = expiry;
            this.email = email;
        }
    }

    @GetMapping("/Login_Sigin")
    public String loginForm(Model model,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Authentication authentication) {
        // Nếu đã đăng nhập thì chuyển về trang index
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            return "redirect:/index";
        }
        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng, hoặc tài khoản chưa được kích hoạt.");
        }
        if (logout != null) {
            model.addAttribute("success", "Bạn đã đăng xuất thành công.");
        }
        model.addAttribute("cartCount", 0);
        return "Login_Signin";
    }

    // REMOVE THIS @PostMapping("/login") METHOD
    // Spring Security's formLogin will handle the POST /login request
    // automatically.
    // It will redirect to defaultSuccessUrl on success or failureUrl on failure.

    @PostMapping("/register")
    public String registerUser(@RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu và xác nhận mật khẩu không khớp.");
            model.addAttribute("cartCount", 0);
            return "Login_Signin";
        }

        if (usrSer.getUserByEmail(email).isPresent()) {
            model.addAttribute("error", "Email đã được sử dụng.");
            model.addAttribute("cartCount", 0);
            return "Login_Signin";
        }

        try {
            User newUser = new User();
            newUser.setHoTen(fullName);
            newUser.setEmail(email);
            String generatedTenNguoiDung = email.split("@")[0];
            newUser.setTenNguoiDung(generatedTenNguoiDung + "_" + new Random().nextInt(10000)); // Adds a number 0-9999
                                                                                                // for uniqueness

            // Use the injected PasswordEncoder to hash the password
            newUser.setMatKhau(passwordEncoder.encode(password));
            newUser.setVaiTro(false); // Default to "Học viên" (User)
            newUser.setStatus(true); // Account is active by default

            usrSer.createUser(newUser);

            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            model.addAttribute("cartCount", 0);
            return "Login_Signin";
        } catch (ConstraintViolationException e) {
            model.addAttribute("error", "Dữ liệu không hợp lệ: " + e.getMessage());
            model.addAttribute("cartCount", 0);
            return "Login_Signin";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Lỗi khi đăng ký: " + e.getMessage());
            model.addAttribute("cartCount", 0);
            return "Login_Signin";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Spring Security handles logout, this method will be intercepted by it.
        // You can keep it if you need to add custom logic *before* Spring Security's
        // logout
        // or just rely entirely on Spring Security's logout. For simple cases, you can
        // remove this method
        // as Spring Security's SecurityConfig logout setup is sufficient.
        // For now, we'll keep it as a placeholder and let Spring Security's
        // SecurityFilterChain handle the actual invalidation.
        return "redirect:/index";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("error", null);
        model.addAttribute("success", null);
        return "Forgot_Password";
    }

    @PostMapping("/forgot-password")
    public String sendResetCode(@RequestParam String email, Model model) {
        Optional<User> userOpt = usrSer.getUserByEmail(email);
        if (!userOpt.isPresent()) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống!");
            return "Forgot_Password";
        }

        String code = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);
        resetTokens.put(email, new ResetToken(code, expiry, email));

        sendResetCodeEmail(email, code);
        model.addAttribute("success", "Mã xác nhận đã được gửi đến email của bạn!");
        model.addAttribute("email", email);
        return "Change_Password";
    }

    @GetMapping("/changePass")
    public String showResetPasswordForm(Model model) {
        model.addAttribute("error", null);
        model.addAttribute("success", null);
        return "Change_Password";
    }

    @PostMapping("/changePass")
    public String resetPassword(@RequestParam String email,
            @RequestParam String code,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu và xác nhận mật khẩu không khớp!");
            return "Change_Password";
        }

        if (!password.matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}")) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số!");
            return "Change_Password";
        }

        ResetToken token = resetTokens.get(email);
        if (token == null || !token.code.equals(code)) {
            model.addAttribute("error", "Mã xác nhận không hợp lệ!");
            return "Change_Password";
        }

        if (token.expiry.isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Mã xác nhận đã hết hạn!");
            resetTokens.remove(email);
            return "Change_Password";
        }

        Optional<User> userOpt = usrSer.getUserByEmail(email);
        if (!userOpt.isPresent()) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống!");
            return "Change_Password";
        }

        User user = userOpt.get();
        user.setMatKhau(passwordEncoder.encode(password)); // Use PasswordEncoder here
        usrSer.updateUser(user.getIdNguoiDung(), user);
        resetTokens.remove(email);

        model.addAttribute("success", "Mật khẩu đã được đặt lại thành công!");
        return "Login_Signin";
    }

    private void sendResetCodeEmail(String email, String code) {
        String subject = "Mã Xác Nhận Đặt Lại Mật Khẩu - DEVNEST LEARNING";
        String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<div style='padding: 20px; background-color: #ffffff; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>"
                +
                "<h2 style='color: #1a1a1a; text-align: center;'>Mã Xác Nhận Đặt Lại Mật Khẩu</h2>" +
                "<p style='color: #333333;'>Chào bạn,</p>" +
                "<p style='color: #333333;'>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu từ bạn. Dưới đây là mã xác nhận của bạn:</p>"
                +
                "<div style='text-align: center; margin: 20px 0;'>" +
                "<p style='font-size: 1.2rem; font-weight: bold; color: #d4af37;'>" + code + "</p>" +
                "</div>" +
                "<p style='color: #333333;'>Vui lòng nhập mã này để đặt lại mật khẩu. Mã có hiệu lực trong 30 phút.</p>"
                +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='http://localhost:8080/changePass' style='background-color: #1a1a1a; color: #ffffff; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;'>ĐẶT LẠI MẬT KHẨU</a>"
                +
                "</div>" +
                "<p style='color: #666666;'>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng liên hệ với chúng tôi qua email: support@DEVNEST LEARNING.com</p>"
                +
                "</div>" +
                "<div style='text-align: center; padding: 20px; color: #666666;'>" +
                "<p style='margin: 5px 0;'><strong>DEVNEST LEARNING</strong></p>" +
                "<p style='margin: 5px 0;'>Hệ thống quản lý học tập</p>" +
                "<p style='margin: 5px 0;'>Email: support@DEVNEST LEARNING.com</p>" +
                "</div>" +
                "</div>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/change-password-authenticated")
    public String showChangePasswordAuthenticatedForm(Model model) { // Remove HttpSession session parameter
        // Get authentication object from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated and is a UserDetails instance (which it should
        // be if logged in via Spring Security)
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetails)) {
            model.addAttribute("error", "Vui lòng đăng nhập để đổi mật khẩu!");
            return "Login_Signin"; // Redirect to login if not authenticated
        }

        // Get the email (username) from the authenticated principal
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Retrieve the full User object from your database using the email
        Optional<User> userOpt = usrSer.getUserByEmail(userEmail);
        if (!userOpt.isPresent()) {
            // This scenario is unlikely if authentication succeeded, but good for
            // robustness
            model.addAttribute("error", "Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại.");
            return "Login_Signin";
        }
        User user = userOpt.get(); // You now have the actual User object from the database

        // You might want to pass the user's current email to the form if needed
        model.addAttribute("userEmail", user.getEmail()); // For displaying purposes on the form if desired
        model.addAttribute("error", null);
        model.addAttribute("success", null);
        return "Change_Password_Authenticated";
    }

    @PostMapping("/change-password-authenticated")
    public String changePasswordAuthenticated(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) { // Bỏ HttpSession session
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return "redirect:/Login_Sigin";
        }
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();

        Optional<User> userOpt = usrSer.getUserByEmail(userEmail);
        if (!userOpt.isPresent()) {
            return "redirect:/Login_Sigin"; // User không tồn tại sau khi xác thực
        }
        User user = userOpt.get();

        if (!passwordEncoder.matches(currentPassword, user.getMatKhau())) {
            model.addAttribute("error", "Mật khẩu hiện tại không đúng!");
            return "Change_Password_Authenticated";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return "Change_Password_Authenticated";
        }

        if (!newPassword.matches("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}")) {
            model.addAttribute("error", "Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số!");
            return "Change_Password_Authenticated";
        }

        user.setMatKhau(passwordEncoder.encode(newPassword));
        usrSer.updateUser(user.getIdNguoiDung(), user);

        // Đặt lại Authentication trong SecurityContext để phản ánh mật khẩu mới băm
        // (không bắt buộc nhưng là cách tốt)
        // Hoặc đơn giản là buộc người dùng đăng xuất và đăng nhập lại
        SecurityContextHolder.clearContext(); // Xóa context hiện tại sau khi đổi mật khẩu
        model.addAttribute("success", "Mật khẩu đã được đổi thành công! Vui lòng đăng nhập lại.");
        return "Login_Signin"; // Chuyển hướng đến trang đăng nhập
    }
}