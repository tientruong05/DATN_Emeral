package com.poly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

public class testController {
	@RequestMapping({"index"})
	public String index() {
		
		return "index";
	}
	
	@RequestMapping("profile")
	public String profile() {
		
		return "Profile";
	}
	
	@RequestMapping("cart")
	public String cart() {
		
		return "cart";
	}
	
	@RequestMapping("changePass")
	public String change_Password() {
		
		return "Change_Password";
	}
	
	@RequestMapping("course_content")
	public String course_content() {
		
		return "course_content";
	}
	
	@RequestMapping("Crud_Categories")
	public String Crud_Categories() {
		return "Crud_Categories";
	}
	
	@RequestMapping("Crud_Course")
	public String Crud_Course() {
		return "Crud_Course";
	}
	
	@RequestMapping("Crud_Sale")
	public String Crud_Sale() {
		return "Crud_Sale";
	}
	
	@RequestMapping("Crud_User")
	public String Crud_User() {
		return "Crud_User";
	}
	
	@RequestMapping("forgotPass")
	public String forgotPass() {
		return "Forgot_Password";
	}
	
	@RequestMapping("Login_Signin")
	public String Login_Signin() {
		return "Login_Signin";
	}
	
	@RequestMapping("payment")
	public String payment() {
		return "Payment";
	}
	
}
