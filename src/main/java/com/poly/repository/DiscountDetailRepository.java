package com.poly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.poly.entity.DiscountDetail;

import java.util.Optional;

public interface DiscountDetailRepository extends JpaRepository<DiscountDetail, Long> {
}