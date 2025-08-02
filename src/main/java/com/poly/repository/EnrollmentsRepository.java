package com.poly.repository;

import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.poly.entity.User;

public interface EnrollmentsRepository extends JpaRepository<Enrollment, Long> {

        @Query("SELECT SUM(e.price) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate")
        Double findTotalRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate")
        Long countEnrollments(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT e.course, SUM(e.price), COUNT(e) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate GROUP BY e.course ORDER BY SUM(e.price) DESC")
        List<Object[]> findTopCoursesByRevenue(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT e.enrollmentDate, SUM(e.price) FROM Enrollment e WHERE e.enrollmentDate BETWEEN :startDate AND :endDate GROUP BY e.enrollmentDate")
        List<Object[]> findRevenueByDate(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e JOIN e.course.category c WHERE c.tenDanhMuc = :tenDanhMuc AND e.enrollmentDate BETWEEN :startDate AND :endDate")
        Long countEnrollmentsByCategory(@Param("tenDanhMuc") String tenDanhMuc,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.course.ID_khoa_hoc = :courseId")
        Enrollment findByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

        @Query("SELECT e.course FROM Enrollment e WHERE e.user.idNguoiDung = :userId")
        List<Course> findCoursesByUserId(@Param("userId") Long userId);

        @Modifying
        @Query("UPDATE Enrollment e SET e.progress = :progress WHERE e.user.id = :userId AND e.course.ID_khoa_hoc = :courseId")
        void updateProgress(@Param("userId") Long userId, @Param("courseId") Long courseId,
                        @Param("progress") Float progress);

        @Query("SELECT e.progress FROM Enrollment e WHERE e.user.id = :userId AND e.course.ID_khoa_hoc = :courseId")
        Float findProgressByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

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
        @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e")
        long countDistinctUsers();

        // Count distinct active students (enrollments where 'status' in Enrollment entity is TRUE)
        @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e WHERE e.status = true")
        long countDistinctActiveUsers();

        // Count distinct completed students (enrollments where finishDate is NOT NULL)
        @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e WHERE e.finishDate IS NOT NULL")
        long countDistinctCompletedUsers();

        // NEW: Count all enrollments with status = true (to match countByStatusIsTrue() if used generally)
        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.status = true")
        long countAllActiveEnrollments(); // Or you could name it countByStatusIsTrue() if you prefer exact match

        // NEW: Count all enrollments with finishDate IS NOT NULL (to match countByFinishDateIsNotNull() if used generally)
        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.finishDate IS NOT NULL")
        long countAllFinishedEnrollments(); // Or you could name it countByFinishDateIsNotNull() if you prefer exact match


        // Count enrollments for a specific course
        long countByCourse(Course course);

        // Count active enrollments for a specific course (based on Enrollment.status = true)
        long countByCourseAndStatusIsTrue(Course course);

        // Count completed enrollments for a specific course (based on Enrollment.finishDate IS NOT NULL)
        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course = :course AND e.finishDate IS NOT NULL")
        long countByCourseAndFinishDateIsNotNull(@Param("course") Course course);

        // Find all enrollments based on the 'status' field (true for đang học, false for đã kết thúc)
        List<Enrollment> findByStatus(Boolean status);
        List<Enrollment> findByFinishDateIsNotNull();
        // Counts enrollments that finished within a specific date range
        // This one queries 'finishDate' (LocalDate)
        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.finishDate BETWEEN :startDate AND :endDate")
        long countByFinishDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate); // <-- CORRECT: LocalDate

        // This one queries 'enrollmentDate' (LocalDateTime) AND 'finishDate' (LocalDate)
        // The previous error was that service passed LocalDate, but this expected LocalDateTime.
        // If you need precise time for 'enrollmentDate', then this method's parameters MUST be LocalDateTime.
        // Since 'enrollmentDate' has a time component, it's safer to keep it LocalDateTime for this query.
        // We will adjust the service to pass LocalDateTime to this.
        @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e " +
                "WHERE e.status = true AND e.enrollmentDate <= :endDate " +
                "AND (e.finishDate IS NULL OR e.finishDate > :startDate)")
        long countDistinctActiveUsersInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDateTime endDate);

        // This one also queries 'enrollmentDate' (LocalDateTime)
        // Similarly, keep LocalDateTime here.
        @Query("SELECT COUNT(DISTINCT e.user.idNguoiDung) FROM Enrollment e " +
                "WHERE e.status = false AND e.finishDate IS NULL " +
                "AND e.enrollmentDate BETWEEN :startDate AND :endDate")
        long countDistinctUnfinishedUsersInRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate); // <-- CORRECT: LocalDateTime (reverted this one)

        @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e")
        long countDistinctStudentsEverEnrolled();
}