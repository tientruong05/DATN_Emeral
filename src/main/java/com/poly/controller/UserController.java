package com.poly.controller;

import com.poly.entity.User;
import com.poly.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Controller
public class UserController {

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;

    @GetMapping("/Crud_User")
    public String showUserManagement(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userService.getAllUsers(pageable);
        model.addAttribute("userPage", userPage);
        model.addAttribute("user", new User());
        return "Crud_User";
    }

    @PostMapping("/Crud_User/add")
    public String addUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Kiểm tra lỗi từ validation
        if (result.hasErrors()) {
            Page<User> userPage = userService.getAllUsers(PageRequest.of(page, size));
            model.addAttribute("userPage", userPage);
            model.addAttribute("showAddModal", true);
            return "Crud_User";
        }

        // Xử lý ngaySinh từ chuỗi sang LocalDate
        if (user.getNgaySinh() != null && user.getNgaySinh().toString().trim().isEmpty()) {
            user.setNgaySinh(null);
        }

        try {
            userService.createUser(user);
            model.addAttribute("message", "Thêm người dùng thành công!");
        } catch (RuntimeException e) {
            result.addError(new org.springframework.validation.FieldError("user", "email", e.getMessage()));
            Page<User> userPage = userService.getAllUsers(PageRequest.of(page, size));
            model.addAttribute("userPage", userPage);
            model.addAttribute("showAddModal", true);
            return "Crud_User";
        }
        return "redirect:/Crud_User";
    }

    // Thêm vào đầu class

    @PostMapping("/Crud_User/update/{id}")
    public String updateUser(@PathVariable Long id, @Valid @ModelAttribute("user") User user, BindingResult result,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (result.hasErrors()) {
            Page<User> userPage = userService.getAllUsers(PageRequest.of(page, size));
            model.addAttribute("userPage", userPage);
            model.addAttribute("showEditModal", true);
            return "Crud_User";
        }

        // Xử lý ngaySinh từ chuỗi sang LocalDate
        if (user.getNgaySinh() != null && user.getNgaySinh().toString().trim().isEmpty()) {
            user.setNgaySinh(null);
        }

        try {
            // Lấy user cũ từ DB
            User oldUser = userService.getUserById(id);
            // Nếu mật khẩu để trống thì giữ mật khẩu cũ
            if (user.getMatKhau() == null || user.getMatKhau().trim().isEmpty()) {
                user.setMatKhau(oldUser.getMatKhau());
            } else {
                // Nếu nhập mật khẩu mới thì băm lại
                user.setMatKhau(passwordEncoder.encode(user.getMatKhau()));
            }

            userService.updateUser(id, user);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email")) {
                result.addError(new org.springframework.validation.FieldError("user", "email", e.getMessage()));
            } else {
                model.addAttribute("error", e.getMessage());
            }
            Page<User> userPage = userService.getAllUsers(PageRequest.of(page, size));
            model.addAttribute("userPage", userPage);
            model.addAttribute("showEditModal", true);
            return "Crud_User";
        }
        return "redirect:/Crud_User";
    }

    @GetMapping("/Crud_User/delete/{id}")
    public String deleteUser(@PathVariable Long id, Model model) {
        try {
            userService.deleteUser(id);
            model.addAttribute("message", "Xóa người dùng thành công!");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/Crud_User";
    }

    @PostMapping("/Crud_User/import")
    public String importUsersFromExcel(@RequestParam("file") MultipartFile file, Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            String result = userService.importUsersFromExcel(file);
            model.addAttribute("message", result);
        } catch (IOException e) {
            model.addAttribute("error", "Lỗi khi nhập file Excel: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        Page<User> userPage = userService.getAllUsers(PageRequest.of(page, size));
        model.addAttribute("userPage", userPage);
        model.addAttribute("user", new User());
        return "Crud_User";
    }

    @GetMapping("/Crud_User/export")
    public ResponseEntity<ByteArrayResource> exportUsersToExcel() throws IOException {
        byte[] excelBytes = userService.exportUsersToExcel();
        ByteArrayResource resource = new ByteArrayResource(excelBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(excelBytes.length)
                .body(resource);
    }
}