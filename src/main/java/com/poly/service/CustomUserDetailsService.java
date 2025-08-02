package com.poly.service;

import com.poly.entity.User;
import com.poly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // Đảm bảo bạn có UserRepository

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Attempting to load user with email: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("User not found with email: " + email);
                    return new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email);
                });

        System.out.println("User found: " + user.getEmail() + ", Status: " + user.getStatus() + ", Role: "
                + user.getVaiTroAsString());
        if (!user.getStatus()) {
            System.out.println("Account is inactive for user: " + user.getEmail());
            throw new UsernameNotFoundException("Tài khoản của bạn đã bị khóa hoặc chưa được kích hoạt.");
        }

        String role = "ROLE_" + user.getVaiTroAsString();
        System.out.println("User authorities: " + Collections.singletonList(new SimpleGrantedAuthority(role)));

        return new CustomUserDetails(
                user,
                Collections.singletonList(new SimpleGrantedAuthority(role)));
    }
}