package com.poly.controller;

import com.poly.entity.User;
import com.poly.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Controller
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfileForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return "redirect:/Login_Signin";
        }
        
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        Optional<User> userOpt = userService.getUserByEmail(userEmail);
        if (!userOpt.isPresent()) {
            logger.error("User not found for email: {}", userEmail);
            return "redirect:/Login_Signin";
        }
        User user = userOpt.get();

        // Đảm bảo tất cả các trường được khởi tạo để tránh null
        if (user.getHoTen() == null) user.setHoTen("");
        if (user.getSoDienThoai() == null) user.setSoDienThoai("");
        if (user.getNgaySinh() == null) user.setNgaySinh(null); // Hoặc giá trị mặc định nếu cần

        model.addAttribute("user", user);
        model.addAttribute("ngaySinh", user.getNgaySinh() != null 
                ? user.getNgaySinh().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) 
                : "");
        return "Profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User updatedUser, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return "redirect:/Login_Signin";
        }

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        Optional<User> existingUserOpt = userService.getUserByEmail(email);
        if (!existingUserOpt.isPresent()) {
            return "redirect:/Login_Signin";
        }

        User existingUser = existingUserOpt.get();

        // Giữ lại các giá trị không được phép chỉnh sửa
        updatedUser.setIdNguoiDung(existingUser.getIdNguoiDung());
        updatedUser.setEmail(existingUser.getEmail());
        updatedUser.setTenNguoiDung(existingUser.getTenNguoiDung());
        updatedUser.setMatKhau(existingUser.getMatKhau());
        updatedUser.setVaiTro(existingUser.getVaiTro());
        updatedUser.setAnhDaiDien(existingUser.getAnhDaiDien());
        updatedUser.setStatus(existingUser.getStatus());
        updatedUser.setDiaChi(existingUser.getDiaChi());

        // Validate nếu muốn
        if (updatedUser.getHoTen() == null || updatedUser.getHoTen().isEmpty()) {
            model.addAttribute("error", "Họ tên không được để trống");
            return "Profile";
        }

        try {
            userService.updateUser(updatedUser.getIdNguoiDung(), updatedUser);
            model.addAttribute("success", "Cập nhật thông tin thành công");
            model.addAttribute("user", updatedUser);
        } catch (Exception e) {
            model.addAttribute("error", "Cập nhật thất bại: " + e.getMessage());
            model.addAttribute("user", existingUser);
        }

        return "Profile";
    }

}