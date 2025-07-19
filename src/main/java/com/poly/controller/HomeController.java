package com.poly.controller;

import com.poly.entity.Category;
import com.poly.entity.Course;
import com.poly.entity.User;
import com.poly.repository.CourseRepository;
import com.poly.repository.EnrollmentsRepository;
import com.poly.service.CategoryService;
import com.poly.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CategoryService categoryService;

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
    
    @RequestMapping("/search")
    public String searchAll (Model model,
            @RequestParam("q") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
    	Page<Course> courses = courseService.findSearchAll(search, pageable);
    	
    	
    	model.addAttribute("currentPage", courses.getNumber());
        model.addAttribute("totalPages", courses.getTotalPages());
        model.addAttribute("pageSize", courses.getSize());
        model.addAttribute("totalItems", courses.getTotalElements());
        model.addAttribute("courses", courses.getContent());
        model.addAttribute("search", search);
    	return "search";
    }
    
    @RequestMapping("/list")
    public String listCourses(@RequestParam(value = "cateId", required = false) Integer cateId,
                              Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "8") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Course> courses;

        String pageTitle = "Tất cả khóa học";

        if (cateId != null) {
            courses = courseRepository.findByIdCate(cateId, pageable);
            Category category = categoryService.getCategoryById(cateId); // Bạn cần tạo method này
            if (category != null) {
                pageTitle = category.getTenDanhMuc();
            }
        } else {
            courses = courseRepository.findAll(pageable);
        }

        model.addAttribute("currentPage", courses.getNumber());
        model.addAttribute("totalPages", courses.getTotalPages());
        model.addAttribute("pageSize", courses.getSize());
        model.addAttribute("totalItems", courses.getTotalElements());
        model.addAttribute("courses", courses.getContent());
        model.addAttribute("pageTitle", pageTitle); // Gửi xuống view

        return "List_Course";
    }
}