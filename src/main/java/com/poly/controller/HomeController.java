package com.poly.controller;

import com.poly.entity.Category;
import com.poly.entity.Course;
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

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private CourseService courseService;

    @GetMapping({ "/", "/index" })
    public String home(@RequestParam(value = "type", required = false, defaultValue = "all") String type, Model model) {
        List<Course> courses = courseService.getCoursesByType(type);
        List<Category> categories = courseService.getAllCate();
        long totalStudents = courseService.getTotalStudents();
        long totalCourses = courseService.getTotalCourses();
        long totalInstructors = courseService.getTotalInstructors();
        model.addAttribute("courses", courses);
        model.addAttribute("categories", categories);
        model.addAttribute("activeType", type);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("totalInstructors", totalInstructors);
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
}