package com.poly.service;

import com.poly.entity.User;
import com.poly.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Thêm phương thức này vào UserService
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

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
        // Gán giá trị mặc định cho tenNguoiDung nếu null
        if (user.getTenNguoiDung() == null) {
            user.setTenNguoiDung(user.getEmail().split("@")[0]); // Lấy phần trước @ của email
        }
        // Không gán giá trị cho so_dien_thoai, giữ NULL nếu không được cung cấp
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        // Kiểm tra email trùng (bỏ qua nếu email không thay đổi)
        if (!user.getEmail().equals(userDetails.getEmail()) && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email đã tồn tại, vui lòng nhập email khác");
        }
        // Gán giá trị mặc định cho tenNguoiDung nếu null
        if (userDetails.getTenNguoiDung() == null) {
            userDetails.setTenNguoiDung(userDetails.getEmail().split("@")[0]);
        }
        // Không gán giá trị cho so_dien_thoai, giữ NULL nếu không được cung cấp
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

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByTenNguoiDung(username);
    }
}