package com.poly.controller;

import com.poly.entity.Category;
import com.poly.entity.Course;
import com.poly.entity.User;
import com.poly.repository.EnrollmentsRepository;
import com.poly.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private CourseService courseService;
    
    @Autowired
    private EnrollmentsRepository enrollmentsRepository;

    @GetMapping({ "/", "/index" })
    public String home(@RequestParam(value = "type", required = false, defaultValue = "all") String type,
                      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                      Model model, HttpSession session) {
        List<Course> courses = courseService.getCoursesByType(type, page);
        List<Category> categories = courseService.getAllCate();
        long totalStudents = courseService.getTotalStudents();
        long totalCourses = courseService.getTotalCourses();
        long totalInstructors = courseService.getTotalInstructors();
        
        // Lấy danh sách khóa học đã đăng ký của người dùng
        List<Course> enrolledCourses = Collections.emptyList();
        User user = (User) session.getAttribute("user");
        if (user != null) {
            enrolledCourses = enrollmentsRepository.findCoursesByUserId(user.getIdNguoiDung());
        }
        
        model.addAttribute("courses", courses);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("categories", categories);
        model.addAttribute("activeType", type);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalInstructors", totalInstructors);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", courseService.getTotalPages(type));
        return "index";
    }
}