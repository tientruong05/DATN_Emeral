package com.poly.controller;

import com.poly.entity.User;
import com.poly.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/Crud_User")
    public String showUserManagement(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("user", new User());
        return "Crud_User";
    }

    @PostMapping("/Crud_User/add")
    public String addUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("showAddModal", true); // Giữ modal mở
            return "Crud_User";
        }
        try {
            userService.createUser(user);
        } catch (RuntimeException e) {
            result.addError(new org.springframework.validation.FieldError("user", "email", e.getMessage()));
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("showAddModal", true); // Giữ modal mở
            return "Crud_User";
        }
        return "redirect:/Crud_User";
    }

    @PostMapping("/Crud_User/update/{id}")
    public String updateUser(@PathVariable Long id, @Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("showEditModal", true); // Giữ modal mở
            return "Crud_User";
        }
        try {
            userService.updateUser(id, user);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Email")) {
                result.addError(new org.springframework.validation.FieldError("user", "email", e.getMessage()));
            } else {
                model.addAttribute("error", e.getMessage());
            }
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("showEditModal", true); // Giữ modal mở
            return "Crud_User";
        }
        return "redirect:/Crud_User";
    }

    @GetMapping("/Crud_User/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/Crud_User";
    }
}