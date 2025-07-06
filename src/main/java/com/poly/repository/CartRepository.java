package com.poly.repository;

import com.poly.entity.Cart;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    @Query("SELECT c FROM Cart c WHERE c.user.idNguoiDung = :userId AND c.status = true")
    List<Cart> findByUserId(Long userId);
    @Modifying
    @Transactional
    @Query("DELETE FROM Cart c WHERE c.user.idNguoiDung = :userId")
    void deleteByUserId(@Param("userId") Long userId);


}