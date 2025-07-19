package com.poly.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    
    @Column(name="progress")
    private String progress; //Lưu ID các video đã học của khóa học
    
 // Phương thức tiện ích để lấy danh sách ID video đã xem
    @Transient
    public List<Long> getCompletedVideoIds() {
        if (progress == null || progress.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(progress.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    // Phương thức tiện ích để tính số video đã hoàn thành
    @Transient
    public long getCompletedLessons() {
        return getCompletedVideoIds().size();
    }

    // Phương thức tiện ích để tính phần trăm tiến độ
    @Transient
    public double getProgressPercentage() {
        if (course == null || course.getVideos() == null || course.getVideos().isEmpty()) {
            return 0.0;
        }
        long totalVideos = course.getVideos().size();
        long completedVideos = getCompletedLessons();
        return totalVideos > 0 ? (completedVideos * 100.0 / totalVideos) : 0.0;
    }
}