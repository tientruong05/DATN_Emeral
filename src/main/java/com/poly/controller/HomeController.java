package com.poly.controller;

import com.poly.entity.Category;
import com.poly.entity.Course;
import com.poly.entity.User; // Ensure this User is your custom User entity that likely implements UserDetails
import com.poly.repository.CourseRepository;
import com.poly.repository.EnrollmentsRepository;
import com.poly.service.CartService;
import com.poly.service.CategoryService;
import com.poly.service.CourseService;
import com.poly.service.CustomUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.security.core.Authentication; // Import Spring Security's Authentication
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.authentication.AnonymousAuthenticationToken; // Import AnonymousAuthenticationToken

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // Keep if you still need to manage cartCount in session
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
    
    @Autowired
    private CartService cartService;
    
    private User getAuthenticatedUser(HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.poly.service.CustomUserDetails customUserDetails) {
                User user = customUserDetails.getUser();
                session.setAttribute("user", user); // đồng bộ lại session nếu cần
                return user;
            }
        }
        return (User) session.getAttribute("user");
    }

    @GetMapping({ "/", "/index" })
    public String home(@RequestParam(value = "type", required = false, defaultValue = "all") String type,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            Model model, HttpSession session) {
        List<Course> courses = courseService.getCoursesByType(type, page);
        List<Category> categories = courseService.getAllCate();
        long totalStudents = courseService.getTotalStudents();
        long totalCourses = courseService.getTotalCourses();
        long totalInstructors = courseService.getTotalInstructors();

        // Lấy user và cập nhật cartCount/enrolledCourses
        List<Course> enrolledCourses = Collections.emptyList();
        int cartCount = 0;
        User authenticatedUser = getAuthenticatedUser(session);
        if (authenticatedUser != null) {
            enrolledCourses = enrollmentsRepository.findCoursesByUserId(authenticatedUser.getIdNguoiDung());
            cartCount = cartService.getCartItemsByUser(authenticatedUser.getIdNguoiDung()).size();
        }
        session.setAttribute("cartCount", cartCount);
        model.addAttribute("cartCount", cartCount);

        model.addAttribute("courses", courses);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("categoriesList", categories);
        model.addAttribute("activeType", type);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalInstructors", totalInstructors);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", courseService.getTotalPages(type));
        return "index";
    }

    @RequestMapping("/search")
    public String searchAll(Model model,
            @RequestParam("q") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            HttpSession session) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> courses = courseService.findSearchAll(search, pageable);

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categoriesList", categories);

        // Lấy user và cập nhật cartCount/enrolledCourses
        List<Course> enrolledCourses = Collections.emptyList();
        int cartCount = 0;
        User authenticatedUser = getAuthenticatedUser(session);
        if (authenticatedUser != null) {
            enrolledCourses = enrollmentsRepository.findCoursesByUserId(authenticatedUser.getIdNguoiDung());
            cartCount = cartService.getCartItemsByUser(authenticatedUser.getIdNguoiDung()).size();
        }
        session.setAttribute("cartCount", cartCount);
        model.addAttribute("cartCount", cartCount);

        model.addAttribute("currentPage", courses.getNumber());
        model.addAttribute("totalPages", courses.getTotalPages());
        model.addAttribute("pageSize", courses.getSize());
        model.addAttribute("totalItems", courses.getTotalElements());
        model.addAttribute("courses", courses.getContent());
        model.addAttribute("search", search);
        model.addAttribute("enrolledCourses", enrolledCourses);
        return "search";
    }

    @RequestMapping("/list")
    public String listCourses(@RequestParam(value = "cateId", required = false) Integer cateId,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            HttpSession session) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Course> courses;
        String pageTitle = "Tất cả khóa học";

        if (cateId != null) {
            courses = courseRepository.findByIdCate(cateId, pageable);
            Category category = categoryService.getCategoryById(cateId);
            if (category != null) {
                pageTitle = category.getTenDanhMuc();
            }
        } else {
            courses = courseRepository.findAll(pageable);
        }

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categoriesList", categories);

        // Lấy user và cập nhật cartCount/enrolledCourses
        List<Course> enrolledCourses = Collections.emptyList();
        int cartCount = 0;
        User authenticatedUser = getAuthenticatedUser(session);
        if (authenticatedUser != null) {
            enrolledCourses = enrollmentsRepository.findCoursesByUserId(authenticatedUser.getIdNguoiDung());
            cartCount = cartService.getCartItemsByUser(authenticatedUser.getIdNguoiDung()).size();
        }
        session.setAttribute("cartCount", cartCount);
        model.addAttribute("cartCount", cartCount);

        model.addAttribute("currentPage", courses.getNumber());
        model.addAttribute("totalPages", courses.getTotalPages());
        model.addAttribute("pageSize", courses.getSize());
        model.addAttribute("totalItems", courses.getTotalElements());
        model.addAttribute("courses", courses.getContent());
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("enrolledCourses", enrolledCourses);

        return "List_Course";
    }
}