package com.poly.repository;

import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EnrollmentsRepository extends JpaRepository<Enrollment, Long> {
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.user.idNguoiDung = :userId")
    List<Enrollment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(e.price) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate")
    Double findTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate")
    Long countEnrollments(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e.course, SUM(e.price), COUNT(e) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate GROUP BY e.course ORDER BY SUM(e.price) DESC")
    List<Object[]> findTopCoursesByRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e.enrollmentDate, SUM(e.price) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate GROUP BY e.enrollmentDate")
    List<Object[]> findRevenueByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e JOIN e.course.category c WHERE c.tenDanhMuc = :tenDanhMuc AND e.enrollmentDate BETWEEN :startDate AND :endDate")
    Long countEnrollmentsByCategory(@Param("tenDanhMuc") String tenDanhMuc, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Enrollment e WHERE e.user.idNguoiDung = :userId AND e.course.ID_khoa_hoc = :courseId")
    Enrollment findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT e.course FROM Enrollment e WHERE e.user.idNguoiDung = :userId")
    List<Course> findCoursesByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Enrollment e SET e.progress = :progress WHERE e.user.idNguoiDung = :userId AND e.course.ID_khoa_hoc = :courseId")
    void updateProgress(@Param("userId") Long userId, @Param("courseId") Long courseId, @Param("progress") Float progress);

    @Query("SELECT e.progress FROM Enrollment e WHERE e.user.idNguoiDung = :userId AND e.course.ID_khoa_hoc = :courseId")
    Float findProgressByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.user = :user")
    Page<Enrollment> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT e FROM Enrollment e WHERE e.user = :user AND LOWER(e.course.ten_khoa_hoc) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Enrollment> findByUserAndCourse_TenKhoaHocContainingIgnoreCase(@Param("user") User user, @Param("query") String query, Pageable pageable);

    long countByUser(User user);

    long countByUserAndFinishDateIsNotNull(User user);

    long countByUserAndFinishDateIsNull(User user);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.ID_khoa_hoc = :courseId")
    Long countTotalEnrollmentsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.ID_khoa_hoc = :courseId AND e.finishDate IS NOT NULL")
    Long countCompletedEnrollmentsByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e")
    long countDistinctUsers();

    @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e WHERE e.status = true")
    long countDistinctActiveUsers();

    @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e WHERE e.finishDate IS NOT NULL")
    long countDistinctCompletedUsers();

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.status = true")
    long countAllActiveEnrollments();

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.finishDate IS NOT NULL")
    long countAllFinishedEnrollments();

    long countByCourse(Course course);

    long countByCourseAndStatusIsTrue(Course course);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course = :course AND e.finishDate IS NOT NULL")
    long countByCourseAndFinishDateIsNotNull(@Param("course") Course course);

    List<Enrollment> findByStatus(Boolean status);

    List<Enrollment> findByFinishDateIsNotNull();

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.finishDate BETWEEN :startDate AND :endDate")
    long countByFinishDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e WHERE e.status = true AND e.enrollmentDate <= :endDate AND (e.finishDate IS NULL OR e.finishDate > :startDate)")
    long countDistinctActiveUsersInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e WHERE e.status = false AND e.finishDate IS NULL AND e.enrollmentDate BETWEEN :startDate AND :endDate")
    long countDistinctUnfinishedUsersInRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e")
    long countDistinctStudentsEverEnrolled();
}