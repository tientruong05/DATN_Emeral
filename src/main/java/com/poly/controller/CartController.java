package com.poly.controller;

import com.poly.entity.Cart;
import com.poly.entity.Course;
import com.poly.entity.User;
import com.poly.service.CartService;
import com.poly.service.CourseService;
import com.poly.service.PayOSService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private PayOSService payOSService;

    @Value("${payos.returnUrl}")
    private String returnUrl;

    @Value("${payos.cancelUrl}")
    private String cancelUrl;

    @GetMapping
    public String viewCart(Model model, HttpSession session,
            @RequestParam(value = "logout", required = false) String logout) {
        User user = (User) session.getAttribute("user");
        List<Cart> cartItems = List.of();
        int cartCount = 0;
        double totalPrice = 0;

        if (user != null) {
            Long userId = user.getIdNguoiDung();
            cartItems = cartService.getCartItemsByUser(userId);
            cartCount = cartItems.size();
            totalPrice = cartItems.stream()
                    .mapToDouble(Cart::getPrice)
                    .sum();
        } else {
            // Nếu logout, xóa cartCount khỏi session và model
            if ("true".equals(logout)) {
                session.setAttribute("cartCount", 0);
                model.addAttribute("cartCount", 0);
                model.addAttribute("message", "Bạn đã đăng xuất. Giỏ hàng đã được làm mới.");
            } else {
                session.setAttribute("cartCount", 0);
                model.addAttribute("cartCount", 0);
            }
        }

        session.setAttribute("cartCount", cartCount);
        model.addAttribute("cartCount", cartCount);

        List<Course> suggestedCourses = courseService.getRandomCourses(4);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("suggestedCourses", suggestedCourses);

        System.out.println("ViewCart - Session ID: " + session.getId() + ", User: "
                + (user != null ? user.getIdNguoiDung() : "null") + ", CartCount: " + cartCount + ", Logout: "
                + logout);

        return "Cart";
    }

    @PostMapping("/add/{courseId}")
    public String addToCart(@PathVariable Long courseId, 
                           HttpSession session, 
                           Model model,
                           @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            session.setAttribute("cartCount", 0);
            model.addAttribute("error", "Vui lòng đăng nhập để thêm vào giỏ hàng.");
            return "redirect:/Login_Sigin";
        }
        boolean added = cartService.addToCart(user.getIdNguoiDung(), courseId);
        int cartCount = cartService.getCartItemsByUser(user.getIdNguoiDung()).size();
        session.setAttribute("cartCount", cartCount);
        String param = added ? "addedToCart=true" : "alreadyInCart=true";
        if (returnUrl != null && !returnUrl.isEmpty()) {
            return "redirect:" + returnUrl + "?" + param;
        }
        return "redirect:/index?" + param;
    }

    @PostMapping("/course/buy/{courseId}")
    public String buyCoursePost(@PathVariable Long courseId, 
                               HttpSession session, 
                               Model model,
                               @RequestParam(value = "returnUrl", required = false) String returnUrl,
                               HttpServletRequest request) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            session.setAttribute("cartCount", 0);
            model.addAttribute("error", "Vui lòng đăng nhập để mua khóa học.");
            return "redirect:/Login_Sigin";
        }
        boolean added = cartService.addToCart(user.getIdNguoiDung(), courseId);
        int cartCount = cartService.getCartItemsByUser(user.getIdNguoiDung()).size();
        session.setAttribute("cartCount", cartCount);
        String param = added ? "addedToCart=true" : "alreadyInCart=true";
        if (returnUrl == null || returnUrl.isEmpty()) {
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isEmpty()) {
                return "redirect:" + referer + (referer.contains("?") ? "&" : "?") + param;
            }
        } else {
            return "redirect:" + returnUrl + "?" + param;
        }
        return "redirect:/index?" + param;
    }

    @GetMapping("/course/buy/{courseId}")
    public String buyCourseGet(@PathVariable Long courseId, 
                              HttpSession session, 
                              Model model,
                              @RequestParam(value = "returnUrl", required = false) String returnUrl,
                              HttpServletRequest request) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            session.setAttribute("cartCount", 0);
            model.addAttribute("error", "Vui lòng đăng nhập để mua khóa học.");
            return "redirect:/Login_Sigin";
        }
        try {
            cartService.addToCart(user.getIdNguoiDung(), courseId);
            int cartCount = cartService.getCartItemsByUser(user.getIdNguoiDung()).size();
            session.setAttribute("cartCount", cartCount);
            
            // Lấy URL gốc từ request nếu không có returnUrl
            if (returnUrl == null || returnUrl.isEmpty()) {
                String referer = request.getHeader("Referer");
                if (referer != null && !referer.isEmpty()) {
                    return "redirect:" + referer + (referer.contains("?") ? "&" : "?") + "addedToCart=true";
                }
            } else {
                return "redirect:" + returnUrl + "?addedToCart=true";
            }
            
            // Mặc định trả về trang index
            return "redirect:/index?addedToCart=true";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            
            if (returnUrl != null && !returnUrl.isEmpty()) {
                return "redirect:" + returnUrl + "?error=" + e.getMessage();
            }
            
            // Lấy URL gốc từ request nếu không có returnUrl
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isEmpty()) {
                return "redirect:" + referer + (referer.contains("?") ? "&" : "?") + "error=" + e.getMessage();
            }
            
            return "redirect:/index?error=" + e.getMessage();
        }
    }

    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Integer id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            session.setAttribute("cartCount", 0);
            model.addAttribute("error", "Vui lòng đăng nhập để xóa khỏi giỏ hàng.");
            return "redirect:/Login_Sigin";
        }
        try {
            cartService.removeFromCart(id);
            int cartCount = cartService.getCartItemsByUser(user.getIdNguoiDung()).size();
            session.setAttribute("cartCount", cartCount);
            return "redirect:/cart";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/checkout")
    public String checkout(@RequestParam(value = "selectedItems", required = false) String selectedItemsStr, 
                          HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            session.setAttribute("cartCount", 0);
            model.addAttribute("error", "Vui lòng đăng nhập để thanh toán.");
            return "redirect:/Login_Sigin";
        }
        
        // Kiểm tra và xử lý danh sách các item được chọn
        if (selectedItemsStr == null || selectedItemsStr.isEmpty()) {
            model.addAttribute("error", "Vui lòng chọn ít nhất một khóa học để thanh toán.");
            return "redirect:/cart";
        }
        
        // Chuyển đổi chuỗi ID thành danh sách Integer
        List<Integer> selectedItemIds;
        try {
            selectedItemIds = java.util.Arrays.stream(selectedItemsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();
        } catch (Exception e) {
            model.addAttribute("error", "Dữ liệu không hợp lệ.");
            return "redirect:/cart";
        }
        
        if (selectedItemIds.isEmpty()) {
            model.addAttribute("error", "Vui lòng chọn ít nhất một khóa học để thanh toán.");
            return "redirect:/cart";
        }
        
        // Lấy tất cả các item trong giỏ hàng
        List<Cart> allCartItems = cartService.getCartItemsByUser(user.getIdNguoiDung());
        
        // Lọc ra các item đã được chọn
        List<Cart> selectedItems = allCartItems.stream()
            .filter(item -> selectedItemIds.contains(item.getId()))
            .toList();
        
        if (selectedItems.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy khóa học được chọn trong giỏ hàng.");
            return "redirect:/cart";
        }
        
        int totalAmount = (int) selectedItems.stream().mapToDouble(Cart::getPrice).sum();
        long orderCode = System.currentTimeMillis(); // Hoặc sinh mã đơn hàng theo logic của bạn
        String description = ("Thanh toan khoa hoc");
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }
        List<ItemData> items = selectedItems.stream().map(cart ->
                ItemData.builder()
                        .name(cart.getCourse().getTen_khoa_hoc())
                        .quantity(1)
                        .price((int) Math.round(cart.getPrice()))
                        .build()
        ).toList();
        
        // Lưu thông tin đơn hàng để xử lý sau khi thanh toán
        cartService.saveOrderInfo(orderCode, user.getIdNguoiDung(), selectedItems);
        
        CheckoutResponseData response = payOSService.createPaymentLink(orderCode, totalAmount, description, items, returnUrl, cancelUrl);
        if (response == null || response.getCheckoutUrl() == null) {
            model.addAttribute("error", "Không thể tạo link thanh toán. Vui lòng thử lại hoặc liên hệ quản trị viên.");
            return "redirect:/cart";
        }
        return "redirect:" + response.getCheckoutUrl();
    }
}