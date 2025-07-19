package com.poly.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "enrollment_date", nullable = false)
    @CreationTimestamp
    private LocalDateTime enrollmentDate;

    @Column(name = "finish_date")
    private LocalDate finishDate;

    @Column(name = "diem")
    private Double diem; // Trường lưu điểm quiz
    
    @Column(name = "order_code")
    private Long orderCode; // Mã đơn hàng từ PayOS
    
    @Column(name = "status")
    private Boolean status; // Trạng thái đăng ký (true: đang học, false: đã kết thúc)
    
    @Column(name = "progress", columnDefinition = "FLOAT DEFAULT 0")
    private Float progress = 0.0f; // Tiến độ học tập (0-100%)

    @ElementCollection
    @CollectionTable(name = "enrollment_completed_videos", joinColumns = @JoinColumn(name = "enrollment_id"))
    @Column(name = "video_id")
    private List<Long> completedVideos = new ArrayList<>();
}