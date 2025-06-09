package com.poly.controller;

import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.poly.entity.User;
import com.poly.service.UserService;

import jakarta.servlet.http.HttpSession;

//@RequestMapping
@Controller
public class LoginController {
	@Autowired
	UserService usrSer;
	
	
	@GetMapping("/Login_Sigin")
    public String loginForm() {
        return "Login_Signin";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("email") String email,
                            @RequestParam("password") String password,
                            Model model, HttpSession session) {

        Optional<User> userLogin = usrSer.getUserByEmail(email);

        if (userLogin.isPresent()) {
            User user = userLogin.get();
            String encodedInputPassword = Base64.getEncoder().encodeToString(password.getBytes());
            if (user.getMatKhau().equals(encodedInputPassword) && user.getStatus()) {
            	

                session.setAttribute("user", user);

                return "redirect:/index";
            }
         else {
            model.addAttribute("error", "Sai mật khẩu hoặc tài khoản chưa được kích hoạt.");
        }
    } else {
        model.addAttribute("error", "Email không tồn tại trong hệ thống.");
    }
        return "Login_Signin";
    }

   
}
