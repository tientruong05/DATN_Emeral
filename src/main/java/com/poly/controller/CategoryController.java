package com.poly.controller;

import com.poly.entity.Category;
import com.poly.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/Crud_Categories")
    public String showCategoryManagement(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());
        return "Crud_Categories";
    }

    @PostMapping("/Crud_Categories/add")
    public String addCategory(@Valid @ModelAttribute("category") Category category, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("showAddModal", true);
            return "Crud_Categories";
        }
        try {
            categoryService.createCategory(category);
        } catch (RuntimeException e) {
            model.addAttribute("tenDanhMucError", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("showAddModal", true);
            return "Crud_Categories";
        }
        return "redirect:/Crud_Categories";
    }

    @PostMapping("/Crud_Categories/update/{id}")
    public String updateCategory(@PathVariable Integer id, @Valid @ModelAttribute("category") Category category, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("showEditModal", true);
            return "Crud_Categories";
        }
        try {
            categoryService.updateCategory(id, category);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Tên danh mục")) {
                model.addAttribute("tenDanhMucError", e.getMessage());
            } else {
                model.addAttribute("error", e.getMessage());
            }
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("showEditModal", true);
            return "Crud_Categories";
        }
        return "redirect:/Crud_Categories";
    }

    @GetMapping("/Crud_Categories/delete/{id}")
    public String deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return "redirect:/Crud_Categories";
    }
}