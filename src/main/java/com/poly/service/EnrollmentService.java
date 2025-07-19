package com.poly.service;

import com.poly.dto.CourseProgressDTO;
import com.poly.entity.Enrollment;
import com.poly.entity.Video;
import com.poly.repository.EnrollmentsRepository;
import com.poly.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentsRepository enrollmentsRepository;
    
    @Autowired
    private VideoRepository videoRepository;

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
    
    /**
     * Lấy thông tin progress của khóa học cho user
     */
    public CourseProgressDTO getCourseProgress(Long userId, Long courseId) {
        Enrollment enrollment = findByUserAndCourse(userId, courseId);
        if (enrollment == null) {
            return null;
        }
        
        // Lấy tổng số video của khóa học
        List<Video> videos = videoRepository.findVideosByCourseId(courseId);
        int totalVideos = videos.size();
        
        // Tính số video đã hoàn thành dựa trên progress
        Float currentProgress = enrollment.getProgress() != null ? enrollment.getProgress() : 0.0f;
        int completedVideos = Math.round((currentProgress / 100.0f) * totalVideos);
        
        // A course is completed only if the finish date is set
        boolean isCompleted = enrollment.getFinishDate() != null;
        
        return new CourseProgressDTO(
            courseId,
            enrollment.getCourse().getTen_khoa_hoc(),
            totalVideos,
            completedVideos,
            currentProgress,
            isCompleted
        );
    }
    
    /**
     * Update progress khi user hoàn thành video
     */
    @Transactional
    public boolean updateProgress(Long userId, Long courseId, Long videoId) {
        try {
            // Kiểm tra enrollment tồn tại
            Enrollment enrollment = findByUserAndCourse(userId, courseId);
            if (enrollment == null) {
                return false;
            }
            
            // Lấy tổng số video của khóa học
            List<Video> videos = videoRepository.findVideosByCourseId(courseId);
            int totalVideos = videos.size();
            
            if (totalVideos == 0) {
                return false;
            }
            
            // Lấy danh sách video đã hoàn thành và cập nhật
            List<Long> completedVideos = enrollment.getCompletedVideos();
            if (!completedVideos.contains(videoId)) {
                completedVideos.add(videoId);
            }
            enrollment.setCompletedVideos(completedVideos);

            // Tính toán lại progress dựa trên số video đã hoàn thành
            Float newProgress = ((float) completedVideos.size() / totalVideos) * 100.0f;
            
            // Update progress và lưu lại enrollment với danh sách video đã hoàn thành
            enrollment.setProgress(newProgress);
            enrollmentsRepository.save(enrollment);
            
            // The finish_date will be set after passing the quiz.
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Lấy progress hiện tại của user cho khóa học
     */
    public Float getCurrentProgress(Long userId, Long courseId) {
        return enrollmentsRepository.findProgressByUserIdAndCourseId(userId, courseId);
    }
    
    /**
     * Kiểm tra xem khóa học đã hoàn thành chưa
     */
    /**
     * Explicitly marks a course as completed by setting the finish date.
     */
    @Transactional
    public void completeCourse(Long userId, Long courseId) {
        Enrollment enrollment = findByUserAndCourse(userId, courseId);
        if (enrollment != null && enrollment.getFinishDate() == null) {
            enrollment.setFinishDate(java.time.LocalDate.now());
            // Ensure progress is 100% when completing
            if (enrollment.getProgress() < 100.0f) {
                enrollment.setProgress(100.0f);
            }
            enrollmentsRepository.save(enrollment);
        }
    }

    public boolean isCourseCompleted(Long userId, Long courseId) {
        CourseProgressDTO progressInfo = getCourseProgress(userId, courseId);
        boolean isCompleted = progressInfo != null && progressInfo.isCompleted();
        System.out.println("DEBUG: isCourseCompleted - userId: " + userId + ", courseId: " + courseId + 
                          ", progressInfo: " + progressInfo + ", isCompleted: " + isCompleted);
        return isCompleted;
    }
}