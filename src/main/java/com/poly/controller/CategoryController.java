package com.poly.controller;

import com.poly.entity.Category;
import com.poly.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // <- thêm dòng này
import org.springframework.data.domain.PageRequest; // <- thêm dòng này
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/Crud_Categories")
    public String showCategoryManagement(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Category> categoryPage = categoryService.getAllCategories(PageRequest.of(page, size));
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("category", new Category());
        return "Crud_Categories";
    }

    @PostMapping("/Crud_Categories/add")
    public String addCategory(@Valid @ModelAttribute("category") Category category, BindingResult result, Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (result.hasErrors()) {
            Page<Category> categoryPage = categoryService.getAllCategories(PageRequest.of(page, size));
            model.addAttribute("categoryPage", categoryPage);
            model.addAttribute("showAddModal", true);
            return "Crud_Categories";
        }
        try {
            categoryService.createCategory(category);
        } catch (RuntimeException e) {
            model.addAttribute("tenDanhMucError", e.getMessage());
            Page<Category> categoryPage = categoryService.getAllCategories(PageRequest.of(page, size));
            model.addAttribute("categoryPage", categoryPage);
            model.addAttribute("showAddModal", true);
            return "Crud_Categories";
        }
        return "redirect:/Crud_Categories";
    }

    @PostMapping("/Crud_Categories/update/{id}")
    public String updateCategory(@PathVariable Integer id, @Valid @ModelAttribute("category") Category category,
            BindingResult result, Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (result.hasErrors()) {
            Page<Category> categoryPage = categoryService.getAllCategories(PageRequest.of(page, size));
            model.addAttribute("categoryPage", categoryPage);
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
            Page<Category> categoryPage = categoryService.getAllCategories(PageRequest.of(page, size));
            model.addAttribute("categoryPage", categoryPage);
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