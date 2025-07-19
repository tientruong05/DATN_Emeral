package com.poly.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.poly.entity.Category;
import com.poly.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class MenuController {
    
    @Autowired
    private CategoryService categoryService;

    @ModelAttribute("categoriesList")
    public List<Category> getCategories() {
        return categoryService.getCategoriesByStatus();
    }

}