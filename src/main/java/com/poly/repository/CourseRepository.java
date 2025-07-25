package com.poly.repository;

import com.poly.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    // Sắp xếp theo số lượng đăng ký giảm dần
    @Query("SELECT c FROM Course c LEFT JOIN c.enrollments e WHERE c.status = true GROUP BY c ORDER BY COUNT(e) DESC")
    List<Course> findTopNByStatusTrue(@Param("limit") int limit);

    // Sắp xếp theo số lượng đăng ký giảm dần
    @Query("SELECT c FROM Course c LEFT JOIN c.enrollments e WHERE c.status = true AND c.category.tenDanhMuc = :tenDanhMuc GROUP BY c ORDER BY COUNT(e) DESC")
    List<Course> findByCategoryTenDanhMucAndStatusTrue(@Param("tenDanhMuc") String tenDanhMuc);

    // Sắp xếp theo số lượng đăng ký giảm dần
    @Query("SELECT c FROM Course c LEFT JOIN c.enrollments e WHERE c.status = true GROUP BY c ORDER BY COUNT(e) DESC")
    Page<Course> findByStatusTrue(Pageable pageable);

    // Sắp xếp theo số lượng đăng ký giảm dần
    @Query("SELECT c FROM Course c LEFT JOIN c.enrollments e WHERE c.status = true AND c.category.tenDanhMuc = :tenDanhMuc GROUP BY c ORDER BY COUNT(e) DESC")
    Page<Course> findByCategoryTenDanhMucAndStatusTrue(@Param("tenDanhMuc") String tenDanhMuc, Pageable pageable);

    // Sắp xếp theo số lượng đăng ký giảm dần
    @Query("SELECT c FROM Course c LEFT JOIN c.enrollments e WHERE c.status = true AND c.category.tenDanhMuc = :tenDanhMuc GROUP BY c ORDER BY COUNT(e) DESC")
    List<Course> findByStatusTrueAndCategoryTenDanhMuc(@Param("tenDanhMuc") String tenDanhMuc);

    // Cập nhật phương thức findAll để sắp xếp theo số lượng đăng ký
    @Query("SELECT c FROM Course c LEFT JOIN c.enrollments e GROUP BY c ORDER BY COUNT(e) DESC")
    Page<Course> findAll(Pageable pageable);

    // Cập nhật phương thức findByIdCate để sắp xếp theo số lượng đăng ký
    @Query("SELECT c FROM Course c LEFT JOIN c.enrollments e WHERE c.category.id = :idCate GROUP BY c ORDER BY COUNT(e) DESC")
    Page<Course> findByIdCate(@Param("idCate") Integer idCate, Pageable pageable);

    long countByStatusTrue();

    @Query("SELECT COUNT(DISTINCT e.user.id) FROM Enrollment e WHERE e.course.status = true")
    long countDistinctStudents();

    @Query("SELECT c FROM Course c WHERE c.ten_khoa_hoc = :tenKhoaHoc")
    Optional<Course> findByTen_khoa_hoc(@Param("tenKhoaHoc") String tenKhoaHoc);

    @Query("SELECT c FROM Course c WHERE LOWER(c.ten_khoa_hoc) LIKE LOWER(CONCAT('%', :tenKhoaHoc, '%')) AND c.status = true")
    Page<Course> findCoursesByTenKhoaHocAndStatusTrue(@Param("tenKhoaHoc") String tenKhoaHoc, Pageable pageable);
}