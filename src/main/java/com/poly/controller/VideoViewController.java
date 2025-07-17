package com.poly.controller;

import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.Video;
import com.poly.entity.User;
import com.poly.service.CourseService;
import com.poly.service.EnrollmentService;
import com.poly.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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

        // Kiểm tra trạng thái hoàn thành khóa học
        Enrollment enrollment = enrollmentService.findByUserAndCourse(user.getIdNguoiDung(), courseId);
        boolean hasCompletedCourse = enrollment != null && enrollment.getFinishDate() != null;

        model.addAttribute("course", course);
        model.addAttribute("videos", videos);
        model.addAttribute("currentVideo", currentVideo);
        model.addAttribute("isAIChatExpanded", false);
        model.addAttribute("hasCompletedCourse", hasCompletedCourse);
        model.addAttribute("currentVideoIndex", 0);
        model.addAttribute("totalVideos", videos.size());

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

        // Kiểm tra trạng thái hoàn thành khóa học
        Enrollment enrollment = enrollmentService.findByUserAndCourse(user.getIdNguoiDung(), courseId);
        boolean hasCompletedCourse = enrollment != null && enrollment.getFinishDate() != null;

        model.addAttribute("course", course);
        model.addAttribute("videos", videos);
        model.addAttribute("currentVideo", currentVideo);
        model.addAttribute("isAIChatExpanded", false);
        model.addAttribute("hasCompletedCourse", hasCompletedCourse);
        model.addAttribute("currentVideoIndex", currentVideoIndex);
        model.addAttribute("totalVideos", videos.size());

        return "Video_Learning";
    }
}
