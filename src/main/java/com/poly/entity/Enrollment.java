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
    private Double diem;

    @Column(name = "order_code")
    private Long orderCode;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "progress", columnDefinition = "FLOAT DEFAULT 0")
    private Float progress = 0.0f;

    @ElementCollection
    @CollectionTable(name = "enrollment_completed_videos", joinColumns = @JoinColumn(name = "enrollment_id"))
    @Column(name = "video_id")
    private List<Long> completedVideos = new ArrayList<>();

    // Trả về danh sách ID video đã hoàn thành
    @Transient
    public List<Long> getCompletedVideoIds() {
        return completedVideos != null ? completedVideos : List.of();
    }

    // Tính số bài học đã hoàn thành
    @Transient
    public long getCompletedLessons() {
        return getCompletedVideoIds().size();
    }

    // Tính phần trăm tiến độ học
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
