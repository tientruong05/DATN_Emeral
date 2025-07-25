package com.poly.controller;

import com.poly.entity.User;
import com.poly.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // Import này
import org.springframework.security.core.context.SecurityContextHolder; // Import này
import org.springframework.security.core.userdetails.UserDetails; // Import này
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession; // Vẫn cần cho các mục đích khác, nhưng không phải để lấy User
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional; // Cần dùng Optional

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfileForm(Model model) { // Bỏ HttpSession session
        // Lấy thông tin người dùng từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            // Trường hợp không có ai đăng nhập hoặc principal không phải UserDetails
            return "redirect:/Login_Sigin";
        }
        
        // Lấy email từ UserDetails (đây là email bạn đã dùng để tạo UserDetails)
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
        
        // Tìm user entity từ database bằng email
        Optional<User> userOpt = userService.getUserByEmail(userEmail);
        if (!userOpt.isPresent()) {
            // Đây là trường hợp hiếm khi xảy ra nếu đã đăng nhập thành công, nhưng nên xử lý
            return "redirect:/Login_Sigin"; // Hoặc một trang lỗi khác
        }
        User user = userOpt.get();

        String[] hoTenParts = user.getHoTen() != null ? user.getHoTen().trim().split("\\s+", 2)
                : new String[] { "", "" };
        model.addAttribute("user", user);
        model.addAttribute("ho", hoTenParts.length > 0 ? hoTenParts[0] : "");
        model.addAttribute("ten", hoTenParts.length > 1 ? hoTenParts[1] : "");
        // Định dạng ngaySinh thành yyyy-MM-dd cho input type="date"
        model.addAttribute("ngaySinh", user.getNgaySinh() != null 
                ? user.getNgaySinh().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) 
                : "");
        return "Profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam("ho") String ho,
            @RequestParam("ten") String ten,
            @RequestParam("soDienThoai") String soDienThoai,
            @RequestParam("email") String email, // Email có thể thay đổi? Cần cân nhắc lại logic này nếu email là username
            @RequestParam("ngaySinh") String ngaySinh,
            Model model) { // Bỏ HttpSession session
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return "redirect:/Login_Sigin";
        }
        String currentAuthenticatedEmail = ((UserDetails) authentication.getPrincipal()).getUsername();

        // Lấy user từ DB, không phải từ session
        Optional<User> userOpt = userService.getUserByEmail(currentAuthenticatedEmail);
        if (!userOpt.isPresent()) {
             // Lỗi nghiêm trọng: user đã xác thực không tìm thấy trong DB
            return "redirect:/Login_Sigin";
        }
        User user = userOpt.get();

        // Kiểm tra xem email có bị thay đổi và có trùng với người khác không
        if (!email.equals(user.getEmail()) && userService.getUserByEmail(email).isPresent()) {
            model.addAttribute("error", "Email mới đã được sử dụng bởi người khác.");
            model.addAttribute("user", user); // Đảm bảo trả lại thông tin user cũ để tránh mất dữ liệu trên form
            model.addAttribute("ho", ho);
            model.addAttribute("ten", ten);
            model.addAttribute("ngaySinh", ngaySinh);
            return "Profile";
        }


        String hoTen = (ho.trim() + " " + ten.trim()).trim();
        if (hoTen.isEmpty()) {
            model.addAttribute("error", "Họ và tên không được để trống");
            model.addAttribute("user", user);
            model.addAttribute("ho", ho);
            model.addAttribute("ten", ten);
            model.addAttribute("ngaySinh", ngaySinh);
            return "Profile";
        }

        LocalDate parsedNgaySinh = null;
        if (ngaySinh != null && !ngaySinh.trim().isEmpty()) {
            try {
                parsedNgaySinh = LocalDate.parse(ngaySinh, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                model.addAttribute("error", "Ngày sinh không hợp lệ. Vui lòng chọn ngày hợp lệ.");
                model.addAttribute("user", user);
                model.addAttribute("ho", ho);
                model.addAttribute("ten", ten);
                model.addAttribute("ngaySinh", ngaySinh);
                return "Profile";
            }
        }

        user.setHoTen(hoTen);
        user.setSoDienThoai(soDienThoai);
        user.setEmail(email); // Cập nhật email nếu hợp lệ
        user.setNgaySinh(parsedNgaySinh);

        try {
            userService.updateUser(user.getIdNguoiDung(), user);
            // Sau khi cập nhật user, nếu email thay đổi, cần cập nhật lại SecurityContext
            // Nếu email không thay đổi, SecurityContext không cần cập nhật
            // Để đơn giản, và đảm bảo SecurityContext luôn mới nhất,
            // bạn có thể buộc đăng xuất và yêu cầu đăng nhập lại, hoặc cập nhật Authentication object
            // Cách đơn giản nhất để cập nhật nếu email thay đổi: buộc người dùng đăng nhập lại
            if (!currentAuthenticatedEmail.equals(email)) {
                 SecurityContextHolder.clearContext(); // Xóa context hiện tại
                 return "redirect:/Login_Sigin?success=Đổi email thành công, vui lòng đăng nhập lại với email mới.";
            }
            model.addAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi cập nhật thông tin: " + e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("ho", ho);
            model.addAttribute("ten", ten);
            model.addAttribute("ngaySinh", ngaySinh);
            return "Profile";
        }

        // Thay đổi sang return "Profile" để hiển thị thông báo thành công trên trang profile
        // hoặc redirect nếu bạn muốn chuyển hướng
        return "Profile"; // Quay lại trang profile với thông báo thành công
    }
}