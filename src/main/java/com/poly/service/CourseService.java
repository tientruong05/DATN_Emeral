package com.poly.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.entity.Category;
import com.poly.entity.Course;
import com.poly.repository.CategoryRepository;
import com.poly.repository.CourseRepository;

@Service
public class CourseService {
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private CourseRepository courseRepository;
	
	public List<Category> ggetAllCate(){
		return categoryRepository.findAll();
	}
	
	public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course findById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public Course save(Course course) {
        return courseRepository.save(course);
    }

    public void delete(Long id) {
        courseRepository.deleteById(id);
    }
	
	
}
