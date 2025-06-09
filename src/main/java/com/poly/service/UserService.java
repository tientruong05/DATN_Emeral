package com.poly.service;

import com.poly.entity.User;
import com.poly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
    }

    public User createUser(User user) {
        // Kiểm tra email trùng
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại, vui lòng nhập email khác");
        }
        // Gán giá trị duy nhất ngắn cho so_dien_thoai nếu null
        if (user.getSoDienThoai() == null) {
            long timestamp = new Date().getTime();
            user.setSoDienThoai("USER_" + (timestamp % 1000000)); // Lấy 6 chữ số cuối
        }
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        // Kiểm tra email trùng (bỏ qua nếu email không thay đổi)
        if (!user.getEmail().equals(userDetails.getEmail()) && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email đã tồn tại, vui lòng nhập email khác");
        }
        // Gán giá trị duy nhất ngắn cho so_dien_thoai nếu null
        if (userDetails.getSoDienThoai() == null) {
            long timestamp = new Date().getTime();
            userDetails.setSoDienThoai("USER_" + (timestamp % 1000000)); // Lấy 6 chữ số cuối
        }
        user.setTenNguoiDung(userDetails.getTenNguoiDung());
        user.setHoTen(userDetails.getHoTen());
        user.setEmail(userDetails.getEmail());
        user.setMatKhau(userDetails.getMatKhau());
        user.setVaiTro(userDetails.getVaiTro());
        user.setStatus(userDetails.getStatus());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
    
    public Optional<User> getUserByEmail (String email) {
    	return userRepository.findByEmail(email);
    }
}