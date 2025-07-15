package com.poly.service;

import com.poly.entity.Enrollment;
import com.poly.entity.User;
import com.poly.repository.EnrollmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentsRepository enrollmentsRepository;

    public List<Enrollment> findAll() {
        return enrollmentsRepository.findAll();
    }

    public Enrollment findById(Long id) {
        return enrollmentsRepository.findById(id).orElse(null);
    }

    public Enrollment save(Enrollment enrollment) {
        return enrollmentsRepository.save(enrollment);
    }

    public void delete(Long id) {
        enrollmentsRepository.deleteById(id);
    }

    public Enrollment findByUserAndCourse(Long userId, Long courseId) {
        return enrollmentsRepository.findByUserIdAndCourseId(userId, courseId);
    }

    public Double findTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return enrollmentsRepository.findTotalRevenue(startDate, endDate);
    }

    public Long countEnrollments(LocalDateTime startDate, LocalDateTime endDate) {
        return enrollmentsRepository.countEnrollments(startDate, endDate);
    }

    public List<Object[]> findTopCoursesByRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return enrollmentsRepository.findTopCoursesByRevenue(startDate, endDate);
    }

    public List<Object[]> findRevenueByDate(LocalDateTime startDate, LocalDateTime endDate) {
        return enrollmentsRepository.findRevenueByDate(startDate, endDate);
    }

    public Long countEnrollmentsByCategory(String tenDanhMuc, LocalDateTime startDate, LocalDateTime endDate) {
        return enrollmentsRepository.countEnrollmentsByCategory(tenDanhMuc, startDate, endDate);
    }
    
    public Page<Enrollment> findByUser(User user, Pageable pageable) {
        return enrollmentsRepository.findByUser(user, pageable);
    }
    
    public Page<Enrollment> findByUserAndCourseNameContaining(User user, String query, Pageable pageable) {
        return enrollmentsRepository.findByUserAndCourse_TenKhoaHocContainingIgnoreCase(user, query, pageable);
    }
    
    public long countByUser(User user) {
        return enrollmentsRepository.countByUser(user);
    }

    public long countByUserAndCompleted(User user, boolean completed) {
        return completed
                ? enrollmentsRepository.countByUserAndFinishDateIsNotNull(user)
                : enrollmentsRepository.countByUserAndFinishDateIsNull(user);
    }

}