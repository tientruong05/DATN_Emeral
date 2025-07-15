package com.poly.controller;

import com.poly.entity.Cart;
import com.poly.entity.Course;
import com.poly.entity.User;
import com.poly.repository.EnrollmentsRepository;
import com.poly.service.CartService;
import com.poly.service.PayOSService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger logger = Logger.getLogger(PaymentController.class.getName());

    @Autowired
    private PayOSService payOSService;

    @Autowired
    private CartService cartService;
    
    @Autowired
    private EnrollmentsRepository enrollmentsRepository;

    // Xử lý webhook từ PayOS sau khi thanh toán thành công
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(@RequestBody Webhook webhookBody) {
        logger.info("Received webhook from PayOS: " + webhookBody);
        
        try {
            // Xác thực dữ liệu webhook
            WebhookData webhookData = payOSService.verifyPaymentWebhookData(webhookBody);
            logger.info("WebhookData verified: " + (webhookData != null ? "success" : "fail"));
            
            Map<String, String> response = new HashMap<>();
            
            if (webhookData != null && "PAID".equals(webhookData.getCode())) {
                // Xử lý đơn hàng thành công
                // Lấy orderCode từ webhook data để đối chiếu với đơn hàng trong hệ thống
                long orderCode = webhookData.getOrderCode();
                logger.info("Processing payment for order: " + orderCode);
                
                // Gọi phương thức xử lý đơn hàng thành công
                boolean processed = processSuccessfulPayment(orderCode);
                logger.info("Payment processing result: " + (processed ? "success" : "fail"));
                
                if (processed) {
                    response.put("status", "success");
                    response.put("message", "Payment processed successfully");
                    return ResponseEntity.ok(response);
                }
            } else if (webhookData != null) {
                logger.warning("Payment not PAID. Status: " + webhookData.getCode());
            }
            
            response.put("status", "error");
            response.put("message", "Payment verification failed or incomplete");
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            logger.severe("Error processing webhook: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Xử lý khi người dùng quay lại sau khi thanh toán thành công
    @GetMapping("/success")
    public String paymentSuccess(HttpServletRequest request, Model model, HttpSession session) {
        logger.info("User redirected to success page after payment");
        
        // Thường trong thực tế, chức năng này không nên xử lý thanh toán
        // vì việc xử lý đã được thực hiện thông qua webhook
        // Tuy nhiên, trong trường hợp webhook không được gọi (local dev, NAT issues, v.v.)
        // chúng ta có thể xử lý thủ công tại đây
        
        User user = (User) session.getAttribute("user");
        if (user != null) {
            logger.info("Trying manual payment processing for user: " + user.getIdNguoiDung());
            
            // Thử xử lý thanh toán thủ công
            boolean processed = cartService.processManualPayment(user.getIdNguoiDung());
            
            if (processed) {
                logger.info("Manual payment processing successful");
                model.addAttribute("message", "Thanh toán thành công! Cảm ơn bạn đã mua khóa học.");
            } else {
                logger.warning("Manual payment processing failed or no pending orders");
                model.addAttribute("message", "Thanh toán đang được xử lý, vui lòng kiểm tra lại sau ít phút.");
            }
        }
        
        // Nếu có orderCode từ PayOS, ghi log để debug
        String orderCode = request.getParameter("orderCode");
        if (orderCode != null) {
            logger.info("OrderCode from return URL: " + orderCode);
            // Có thể xử lý thêm ở đây nếu cần
        }
        
        return "redirect:/payment/history";
    }

    // Xử lý khi người dùng hủy thanh toán
    @GetMapping("/cancel")
    public String paymentCancel(Model model) {
        logger.info("Payment was cancelled by user");
        
        model.addAttribute("error", "Thanh toán đã bị hủy.");
        return "redirect:/cart";
    }
    
    // Trang lịch sử thanh toán
    @GetMapping("/history")
    public String paymentHistory(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/Login_Signin";
        }
        
        // Lấy lịch sử thanh toán của người dùng và hiển thị
        List<Course> enrolledCourses = enrollmentsRepository.findCoursesByUserId(user.getIdNguoiDung());
        logger.info("Found " + (enrolledCourses != null ? enrolledCourses.size() : 0) + " enrolled courses for user: " + user.getIdNguoiDung());
        
        model.addAttribute("enrolledCourses", enrolledCourses);
        
        return "History_Payment";
    }
    
    // Cung cấp URL alias cho đường dẫn trong header
    @GetMapping("/history-payment")
    public String historyPaymentAlias(Model model, HttpSession session) {
        return paymentHistory(model, session);
    }

    // Phương thức xử lý thanh toán thành công
    private boolean processSuccessfulPayment(long orderCode) {
        try {
            logger.info("Processing payment for orderCode: " + orderCode);
            
            // Xử lý đơn hàng dựa trên orderCode
            // Trong thực tế, bạn cần lưu thông tin đơn hàng và orderCode trong database
            // khi tạo payment link để đối chiếu tại đây
            
            // Đăng ký khóa học cho người dùng
            // Xóa giỏ hàng
            boolean result = cartService.processPaymentByOrderCode(orderCode);
            logger.info("Payment processing result: " + (result ? "success" : "failed"));
            
            return result;
        } catch (Exception e) {
            logger.severe("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 