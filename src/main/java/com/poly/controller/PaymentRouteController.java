package com.poly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller này xử lý các tuyến URL liên quan đến thanh toán ở mức cao nhất
 * để tương thích với các URL được sử dụng trong template
 */
@Controller
public class PaymentRouteController {
    
    /**
     * Alias cho /payment/history để tương thích với URL trong header
     */
    @GetMapping("/history-payment")
    public String historyPaymentAlias() {
        return "redirect:/payment/history";
    }
} 