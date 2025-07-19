package com.poly.controller;

import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.User;
import com.poly.service.CourseService;
import com.poly.service.EnrollmentService;
import com.poly.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/CourseInProgess")
public class MyCourseController {
    private static final Logger log = LoggerFactory.getLogger(MyCourseController.class);

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @GetMapping
    public String showMyCourses(
            @RequestParam(value = "sort", defaultValue = "recent") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            log.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/Login_Signin";
        }

        try {
            Sort sortOption;
            switch (sort) {
                case "progress":
                    sortOption = Sort.by(Sort.Direction.DESC, "diem");
                    break;
                case "name":
                    sortOption = Sort.by(Sort.Direction.ASC, "course.ten_khoa_hoc");
                    break;
                case "completion":
                    sortOption = Sort.by(Sort.Direction.DESC, "diem");
                    break;
                case "recent":
                default:
                    sortOption = Sort.by(Sort.Direction.DESC, "enrollmentDate");
                    break;
            }
            Pageable pageable = PageRequest.of(page, size, sortOption);

            // Lấy tất cả khóa học của người dùng
            Page<Enrollment> enrollmentPage = enrollmentService.findByUser(user, pageable);

            log.info("Số lượng bản ghi Enrollment trả về: {}", enrollmentPage.getContent().size());
            log.info("Trạng thái bản ghi: {}", enrollmentPage.getContent().stream()
                    .map(e -> e.getFinishDate() != null ? "completed" : "in-progress")
                    .collect(Collectors.toList()));

            long totalCourses = enrollmentService.countByUser(user);
            long completedCourses = enrollmentService.countByUserAndCompleted(user, true);
            long inProgressCourses = enrollmentService.countByUserAndCompleted(user, false);

            model.addAttribute("enrollments", enrollmentPage.getContent());

            model.addAttribute("query", "");
            model.addAttribute("sort", sort);
            model.addAttribute("totalItems", enrollmentPage.getTotalElements());
            model.addAttribute("totalPages", enrollmentPage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalCourses", totalCourses);
            model.addAttribute("completedCourses", completedCourses);
            model.addAttribute("inProgressCourses", inProgressCourses);

            log.info("Đã tải trang my-courses cho người dùng {} với sort '{}', trang {}, kích thước {}", 
                    user.getTenNguoiDung(), sort, page, size);

            return "CourseInProgess";
        } catch (Exception e) {
            log.error("Lỗi khi tải trang my-courses cho người dùng {}: {}", user.getTenNguoiDung(), e.getMessage());
            model.addAttribute("error", "Đã xảy ra lỗi khi tải danh sách khóa học. Vui lòng thử lại.");
            return "CourseInProgess";
        }
    }

    @GetMapping("/search")
    public String searchMyCourses(
            @RequestParam(value = "q", defaultValue = "") String query,
            @RequestParam(value = "sort", defaultValue = "recent") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            log.warn("Người dùng chưa đăng nhập, chuyển hướng đến trang đăng nhập");
            return "redirect:/Login_Signin";
        }

        try {
            log.info("Chuỗi tìm kiếm: {}", query);
            Sort sortOption;
            switch (sort) {
                case "progress":
                    sortOption = Sort.by(Sort.Direction.DESC, "diem");
                    break;
                case "name":
                    sortOption = Sort.by(Sort.Direction.ASC, "course.ten_khoa_hoc");
                    break;
                case "completion":
                    sortOption = Sort.by(Sort.Direction.DESC, "diem");
                    break;
                case "recent":
                default:
                    sortOption = Sort.by(Sort.Direction.DESC, "enrollmentDate");
                    break;
            }
            Pageable pageable = PageRequest.of(page, size, sortOption);

            // Tìm kiếm khóa học theo tên
            Page<Enrollment> enrollmentPage = enrollmentService.findByUserAndCourseNameContaining(user, query, pageable);

            log.info("Số lượng bản ghi tìm kiếm trả về: {}", enrollmentPage.getContent().size());
            log.info("Trạng thái bản ghi tìm kiếm: {}", enrollmentPage.getContent().stream()
                    .map(e -> e.getFinishDate() != null ? "completed" : "in-progress")
                    .collect(Collectors.toList()));

            long totalCourses = enrollmentService.countByUser(user);
            long completedCourses = enrollmentService.countByUserAndCompleted(user, true);
            long inProgressCourses = enrollmentService.countByUserAndCompleted(user, false);

            model.addAttribute("enrollments", enrollmentPage.getContent());
            model.addAttribute("query", query);
            model.addAttribute("sort", sort);
            model.addAttribute("totalItems", enrollmentPage.getTotalElements());
            model.addAttribute("totalPages", enrollmentPage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalCourses", totalCourses);
            model.addAttribute("completedCourses", completedCourses);
            model.addAttribute("inProgressCourses", inProgressCourses);

            log.info("Đã tải trang tìm kiếm my-courses cho người dùng {} với query '{}', sort '{}', trang {}, kích thước {}", 
                    user.getTenNguoiDung(), query, sort, page, size);

            return "CourseInProgess";
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm my-courses cho người dùng {}: {}", user.getTenNguoiDung(), e.getMessage());
            model.addAttribute("error", "Đã xảy ra lỗi khi tìm kiếm khóa học. Vui lòng thử lại.");
            return "CourseInProgess";
        }
    }

    @PostMapping("/continue/{enrollmentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> continueCourse(@PathVariable Long enrollmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Enrollment enrollment = enrollmentService.findById(enrollmentId);
            if (enrollment != null) {
                log.info("Tiếp tục khóa học với ID đăng ký {}", enrollmentId);
                response.put("success", true);
                response.put("redirectUrl", "/detail-course/" + enrollment.getCourse().getID_khoa_hoc() + "/learn");
            } else {
                log.error("Không tìm thấy ID đăng ký {}", enrollmentId);
                response.put("success", false);
                response.put("message", "Không tìm thấy khóa học");
            }
        } catch (Exception e) {
            log.error("Lỗi khi tiếp tục khóa học với ID đăng ký {}: {}", enrollmentId, e.getMessage());
            response.put("success", false);
            response.put("message", "Lỗi khi tiếp tục khóa học: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/review/{enrollmentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reviewCourse(@PathVariable Long enrollmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Enrollment enrollment = enrollmentService.findById(enrollmentId);
            if (enrollment != null && enrollment.getFinishDate() != null) {
                log.info("Ôn tập khóa học với ID đăng ký {}", enrollmentId);
                response.put("success", true);
                response.put("redirectUrl", "/detail-course/" + enrollment.getCourse().getID_khoa_hoc() + "/review");
            } else {
                log.error("ID đăng ký {} không tìm thấy hoặc chưa hoàn thành", enrollmentId);
                response.put("success", false);
                response.put("message", "Khóa học chưa hoàn thành hoặc không tồn tại");
            }
        } catch (Exception e) {
            log.error("Lỗi khi ôn tập khóa học với ID đăng ký {}: {}", enrollmentId, e.getMessage());
            response.put("success", false);
            response.put("message", "Lỗi khi ôn tập khóa học: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}