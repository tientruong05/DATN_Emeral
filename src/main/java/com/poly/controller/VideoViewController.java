package com.poly.controller;

import com.poly.dto.ApiResponse;
import com.poly.dto.CourseProgressDTO;
import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.Video;
import com.poly.entity.User;
import com.poly.service.CourseService;
import com.poly.service.EnrollmentService;
import com.poly.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/video-learning")
public class VideoViewController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Hiển thị trang học video với danh sách video của khóa học
     * Nếu không chỉ định video, sẽ hiển thị video đầu tiên
     */
    @GetMapping("/{courseId}")
    public String viewCourseVideos(@PathVariable("courseId") Long courseId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/Login_Signin";
        }

        // Lấy thông tin khóa học
        Course course = courseService.findById(courseId);
        if (course == null) {
            return "redirect:/index";
        }

        // Lấy danh sách video của khóa học
        List<Video> videos = videoService.findByCourseID_khoa_hoc(courseId);
        if (videos.isEmpty()) {
            model.addAttribute("course", course);
            model.addAttribute("videos", videos);
            model.addAttribute("currentVideo", null);
            model.addAttribute("isAIChatExpanded", false);
            model.addAttribute("errorMessage", "Khóa học này chưa có video nào.");
            return "Video_Learning";
        }

        // Lấy video đầu tiên để hiển thị
        Video currentVideo = videos.get(0);

        // Lấy thông tin progress
        CourseProgressDTO progressInfo = enrollmentService.getCourseProgress(user.getIdNguoiDung(), courseId);
        Float currentProgress = progressInfo != null ? progressInfo.progress() : 0.0f;
        
        // Kiểm tra trạng thái hoàn thành khóa học
        boolean hasCompletedCourse = enrollmentService.isCourseCompleted(user.getIdNguoiDung(), courseId);

        model.addAttribute("course", course);
        model.addAttribute("videos", videos);
        model.addAttribute("currentVideo", currentVideo);
        model.addAttribute("isAIChatExpanded", false);
        model.addAttribute("hasCompletedCourse", hasCompletedCourse);
        model.addAttribute("currentVideoIndex", 0);
        model.addAttribute("totalVideos", videos.size());
        model.addAttribute("currentProgress", currentProgress);
        model.addAttribute("progressInfo", progressInfo);

        return "Video_Learning";
    }

    /**
     * Hiển thị một video cụ thể trong khóa học
     */
    @GetMapping("/{courseId}/{videoId}")
    public String viewSpecificVideo(@PathVariable("courseId") Long courseId, 
                                   @PathVariable("videoId") Long videoId, 
                                   Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/Login_Signin";
        }

        // Lấy thông tin khóa học
        Course course = courseService.findById(courseId);
        if (course == null) {
            return "redirect:/index";
        }

        // Lấy danh sách video của khóa học
        List<Video> videos = videoService.findByCourseID_khoa_hoc(courseId);
        if (videos.isEmpty()) {
            model.addAttribute("course", course);
            model.addAttribute("videos", videos);
            model.addAttribute("currentVideo", null);
            model.addAttribute("isAIChatExpanded", false);
            model.addAttribute("errorMessage", "Khóa học này chưa có video nào.");
            return "Video_Learning";
        }

        // Lấy video được chỉ định
        Video currentVideo = videoService.findById(videoId);
        if (currentVideo == null || !currentVideo.getCourse().getID_khoa_hoc().equals(courseId)) {
            // Nếu không tìm thấy video hoặc video không thuộc khóa học này, hiển thị video đầu tiên
            currentVideo = videos.get(0);
        }

        // Tính chỉ số của video hiện tại
        int currentVideoIndex = videos.indexOf(currentVideo);
        if (currentVideoIndex == -1) {
            currentVideoIndex = 0;
            currentVideo = videos.get(0);
        }
        
        // Kiểm tra xem user có quyền xem video này không (theo tuần tự)
        CourseProgressDTO progressInfo = enrollmentService.getCourseProgress(user.getIdNguoiDung(), courseId);
        if (progressInfo != null && currentVideoIndex > progressInfo.completedVideos()) {
            // Nếu user chưa hoàn thành video trước đó, redirect về video đầu tiên chưa hoàn thành
            int nextVideoIndex = progressInfo.completedVideos();
            if (nextVideoIndex < videos.size()) {
                currentVideo = videos.get(nextVideoIndex);
                currentVideoIndex = nextVideoIndex;
            }
        }

        // Kiểm tra trạng thái hoàn thành khóa học
        boolean hasCompletedCourse = enrollmentService.isCourseCompleted(user.getIdNguoiDung(), courseId);
        Float currentProgress = progressInfo != null ? progressInfo.progress() : 0.0f;

        model.addAttribute("course", course);
        model.addAttribute("videos", videos);
        model.addAttribute("currentVideo", currentVideo);
        model.addAttribute("isAIChatExpanded", false);
        model.addAttribute("hasCompletedCourse", hasCompletedCourse);
        model.addAttribute("currentVideoIndex", currentVideoIndex);
        model.addAttribute("totalVideos", videos.size());
        model.addAttribute("currentProgress", currentProgress);
        model.addAttribute("progressInfo", progressInfo);

        return "Video_Learning";
    }
    
    /**
     * API để mark video as completed và update progress
     */
    @PostMapping("/api/mark-completed")
    @ResponseBody
    public ResponseEntity<ApiResponse> markVideoAsCompleted(@RequestParam("courseId") Long courseId,
                                                           @RequestParam("videoId") Long videoId,
                                                           HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Vui lòng đăng nhập để tiếp tục", null));
            }

            // Kiểm tra user có đăng ký khóa học này không
            Enrollment enrollment = enrollmentService.findByUserAndCourse(user.getIdNguoiDung(), courseId);
            if (enrollment == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Bạn chưa đăng ký khóa học này", null));
            }

            // Update progress
            boolean success = enrollmentService.updateProgress(user.getIdNguoiDung(), courseId, videoId);
            if (!success) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Không thể cập nhật tiến độ học tập", null));
            }

            // Lấy thông tin progress mới
            CourseProgressDTO progressInfo = enrollmentService.getCourseProgress(user.getIdNguoiDung(), courseId);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã hoàn thành video!", progressInfo));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Có lỗi xảy ra: " + e.getMessage(), null));
        }
    }
    
    /**
     * API để lấy thông tin progress của khóa học
     */
    @GetMapping("/api/progress/{courseId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> getCourseProgress(@PathVariable("courseId") Long courseId,
                                                        HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Vui lòng đăng nhập để tiếp tục", null));
            }

            CourseProgressDTO progressInfo = enrollmentService.getCourseProgress(user.getIdNguoiDung(), courseId);
            if (progressInfo == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Bạn chưa đăng ký khóa học này", null));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy thông tin tiến độ thành công", progressInfo));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "Có lỗi xảy ra: " + e.getMessage(), null));
        }
    }
}
