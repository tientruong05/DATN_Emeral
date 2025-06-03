package com.poly.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "User")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_nguoi_dung;

    private String ten_nguoi_dung;
    private String ho_ten;
    private String email;
    private String mat_khau;
    private String vai_tro;
    private String anh_dai_dien;
    private LocalDate ngay_sinh;
    private String dia_chi;
    private String so_dien_thoai;
    private String status;

    @OneToMany(mappedBy = "user")
    private List<Enrollment> enrollments;

    @OneToMany(mappedBy = "user")
    private List<Cart> cartItems;
}
