package com.poly.controller;

import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.User;
import com.poly.entity.Video;
import com.poly.repository.EnrollmentsRepository;
import com.poly.service.CourseService;
import com.poly.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/course-detail")
public class CourseDetailController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private EnrollmentsRepository enrollmentsRepository;

    @GetMapping("/{courseId}")
    public String showCourseDetail(@PathVariable("courseId") Long courseId, Model model, HttpSession session) {
        // Lấy thông tin khóa học
        Course course = courseService.findById(courseId);
        if (course == null) {
            return "redirect:/index?error=courseNotFound";
        }

        // Lấy danh sách video của khóa học
        List<Video> videos = videoService.findByCourseID_khoa_hoc(courseId);
        
        // Kiểm tra nếu người dùng đã đăng ký khóa học này chưa
        boolean isEnrolled = false;
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Enrollment enrollment = enrollmentsRepository.findByUserIdAndCourseId(user.getIdNguoiDung(), courseId);
            isEnrolled = (enrollment != null);
        }

        model.addAttribute("course", course);
        model.addAttribute("videos", videos);
        model.addAttribute("isEnrolled", isEnrolled);
        model.addAttribute("videoCount", videos.size());

        // Nếu đã đăng ký và không có video thì truyền thông báo lỗi
        if (isEnrolled && videos.isEmpty()) {
            model.addAttribute("errorMessage", "Khóa học này chưa có video nào.");
        }

        return "course_detail";
    }
} 