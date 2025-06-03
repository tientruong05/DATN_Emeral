package com.poly.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
//    User findByEmail(String email);
}
