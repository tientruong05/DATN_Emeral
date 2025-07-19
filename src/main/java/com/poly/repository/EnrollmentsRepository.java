package com.poly.repository;

import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;


public interface EnrollmentsRepository extends JpaRepository<Enrollment, Long> {

    @Query("SELECT SUM(e.price) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate")
    Double findTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate")
    Long countEnrollments(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e.course, SUM(e.price), COUNT(e) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate GROUP BY e.course ORDER BY SUM(e.price) DESC")
    List<Object[]> findTopCoursesByRevenue(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e.enrollmentDate, SUM(e.price) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate GROUP BY e.enrollmentDate")
    List<Object[]> findRevenueByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e JOIN e.course.category c WHERE c.tenDanhMuc = :tenDanhMuc AND e.enrollmentDate BETWEEN :startDate AND :endDate")
    Long countEnrollmentsByCategory(@Param("tenDanhMuc") String tenDanhMuc, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.course.ID_khoa_hoc = :courseId")
    Enrollment findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);
    
    @Query("SELECT e.course FROM Enrollment e WHERE e.user.idNguoiDung = :userId")
    List<Course> findCoursesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.user = :user")
    Page<Enrollment> findByUser(@Param("user") User user, Pageable pageable);

    
    @Query("SELECT e FROM Enrollment e WHERE e.user = :user AND LOWER(e.course.ten_khoa_hoc) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Enrollment> findByUserAndCourse_TenKhoaHocContainingIgnoreCase(
            @Param("user") User user,
            @Param("query") String query,
            Pageable pageable);
    long countByUser(User user);
    long countByUserAndFinishDateIsNotNull(User user);
    long countByUserAndFinishDateIsNull(User user);
    
 // Đếm tổng số học viên đã đăng ký khóa học
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    Long countTotalEnrollmentsByCourseId(@Param("courseId") Long courseId);

    // Đếm số học viên đã hoàn thành (có ngày kết thúc)
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.finishDate IS NOT NULL")
    Long countCompletedEnrollmentsByCourseId(@Param("courseId") Long courseId);
}