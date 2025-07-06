package com.poly.service;

import com.poly.entity.Cart;
import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.User;
import com.poly.repository.CartRepository;
import com.poly.repository.EnrollmentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class CartService {

    private static final Logger logger = Logger.getLogger(CartService.class.getName());
    
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private EnrollmentsRepository enrollmentsRepository;
    
    // Lưu trữ orderCode và cartItems để sử dụng trong xử lý webhooks
    private final Map<Long, OrderData> pendingOrders = new HashMap<>();

    public List<Cart> getCartItemsByUser(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public void removeFromCart(Integer cartId) {
        cartRepository.deleteById(cartId);
    }

    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }

    public boolean addToCart(Long userId, Long courseId) {
        User user = null;
        try {
            user = userService.getUserById(userId);
        } catch (RuntimeException e) {
            return false;
        }
        Course course = courseService.findById(courseId);
        if (user != null && course != null) {
            // Kiểm tra xem người dùng đã đăng ký khóa học này chưa
            Enrollment existingEnrollment = enrollmentsRepository.findByUserIdAndCourseId(userId, courseId);
            if (existingEnrollment != null) {
                return false; // Đã đăng ký rồi
            }
            // Kiểm tra xem khóa học đã có trong giỏ hàng chưa
            List<Cart> cartItems = getCartItemsByUser(userId);
            boolean alreadyInCart = cartItems.stream()
                    .anyMatch(item -> item.getCourse().getID_khoa_hoc().equals(courseId));
            if (alreadyInCart) {
                return false; // Đã có trong giỏ hàng
            }
            Cart cart = new Cart();
            cart.setUser(user);
            cart.setCourse(course);
            // Use discounted price if available, otherwise use original price
            cart.setPrice(course.isDiscounted() ? course.getDiscountedPrice() : course.getGia_tien());
            cart.setStatus(true);
            save(cart);
            return true;
        }
        return false;
    }
    
    // Lưu thông tin đơn hàng để xử lý webhook
    public void saveOrderInfo(Long orderCode, Long userId, List<Cart> cartItems) {
        logger.info("Saving order info for orderCode: " + orderCode + ", userId: " + userId + ", items: " + cartItems.size());
        pendingOrders.put(orderCode, new OrderData(userId, cartItems));
    }
    
    // Xử lý thanh toán thành công
    public boolean processPaymentByOrderCode(long orderCode) {
        logger.info("Processing payment for orderCode: " + orderCode);
        logger.info("Current pending orders: " + pendingOrders.keySet());
        
        OrderData orderData = pendingOrders.get(orderCode);
        if (orderData == null) {
            logger.warning("No order data found for orderCode: " + orderCode);
            // Không tìm thấy thông tin đơn hàng
            return false;
        }
        
        try {
            Long userId = orderData.getUserId();
            List<Cart> cartItems = orderData.getCartItems();
            User user = userService.getUserById(userId);
            
            logger.info("Found order data for userId: " + userId + " with " + cartItems.size() + " items");
            
            // Đăng ký các khóa học cho người dùng
            for (Cart cart : cartItems) {
                Course course = cart.getCourse();
                
                // Kiểm tra xem người dùng đã đăng ký khóa học này chưa
                Enrollment existingEnrollment = enrollmentsRepository.findByUserIdAndCourseId(userId, course.getID_khoa_hoc());
                if (existingEnrollment == null) {
                    // Tạo đăng ký mới
                    Enrollment enrollment = new Enrollment();
                    enrollment.setUser(user);
                    enrollment.setCourse(course);
                    enrollment.setEnrollmentDate(LocalDateTime.now());
                    enrollment.setPrice(cart.getPrice());
                    enrollment.setOrderCode(orderCode);
                    enrollment.setStatus(true);
                    enrollmentsRepository.save(enrollment);
                    logger.info("Enrolled user: " + userId + " in course: " + course.getID_khoa_hoc());
                } else {
                    logger.info("User: " + userId + " already enrolled in course: " + course.getID_khoa_hoc());
                }
                
                // Xóa item giỏ hàng
                cartRepository.delete(cart);
                logger.info("Removed cart item: " + cart.getId() + " for course: " + course.getID_khoa_hoc());
            }
            
            // Xóa thông tin đơn hàng khỏi danh sách đơn hàng đang chờ
            pendingOrders.remove(orderCode);
            logger.info("Order processed successfully. Removed orderCode: " + orderCode + " from pending orders");
            return true;
        } catch (Exception e) {
            logger.severe("Error processing payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Phương thức thủ công để xử lý thanh toán khi webhook không hoạt động
     * Sử dụng cho trang success khi người dùng quay trở lại sau khi thanh toán
     */
    public boolean processManualPayment(Long userId) {
        logger.info("Manual payment processing for userId: " + userId);
        
        try {
            // Lấy tất cả các đơn hàng đang chờ của người dùng
            for (Map.Entry<Long, OrderData> entry : pendingOrders.entrySet()) {
                OrderData orderData = entry.getValue();
                if (orderData.getUserId().equals(userId)) {
                    long orderCode = entry.getKey();
                    logger.info("Found pending order: " + orderCode + " for userId: " + userId);
                    
                    // Xử lý đơn hàng
                    return processPaymentByOrderCode(orderCode);
                }
            }
            logger.warning("No pending orders found for userId: " + userId);
            return false;
        } catch (Exception e) {
            logger.severe("Error in manual payment processing: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Trả về danh sách tất cả các orderCode đang chờ xử lý
     */
    public List<Long> getAllPendingOrderCodes() {
        return pendingOrders.keySet().stream().toList();
    }
    
    // Lớp để lưu trữ thông tin đơn hàng
    private static class OrderData {
        private final Long userId;
        private final List<Cart> cartItems;
        
        public OrderData(Long userId, List<Cart> cartItems) {
            this.userId = userId;
            this.cartItems = cartItems;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public List<Cart> getCartItems() {
            return cartItems;
        }
    }
    @Transactional
    public void clearCartByUser(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

}