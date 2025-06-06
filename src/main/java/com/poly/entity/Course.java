package com.poly.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID_khoa_hoc;

    private String ten_khoa_hoc;
    private String mo_ta;
    private Double diem_dat;

    @ManyToOne
    @JoinColumn(name = "danh_muc_ID")
    private Category category;

    private Double gia_tien;
    private String anh_dai_dien;
    private boolean status;

    @OneToMany(mappedBy = "course")
    private List<Video> videos;

    @OneToMany(mappedBy = "course")
    private List<Question> questions;

    @OneToMany(mappedBy = "course")
    private List<Enrollment> enrollments;
}
