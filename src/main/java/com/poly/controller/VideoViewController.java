package com.poly.controller;

import com.poly.entity.Course;
import com.poly.entity.Video;
import com.poly.service.CourseService;
import com.poly.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/video-learning")
public class VideoViewController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private VideoService videoService;

    /**
     * Hiển thị trang học video với danh sách video của khóa học
     * Nếu không chỉ định video, sẽ hiển thị video đầu tiên
     */
    @GetMapping("/{courseId}")
    public String viewCourseVideos(@PathVariable("courseId") Long courseId, Model model) {
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

        model.addAttribute("course", course);
        model.addAttribute("videos", videos);
        model.addAttribute("currentVideo", currentVideo);
        model.addAttribute("isAIChatExpanded", false); // Mặc định không mở rộng khung chat AI

        return "Video_Learning";
    }

    /**
     * Hiển thị một video cụ thể trong khóa học
     */
    @GetMapping("/{courseId}/{videoId}")
    public String viewSpecificVideo(@PathVariable("courseId") Long courseId, 
                                   @PathVariable("videoId") Long videoId, 
                                   Model model) {
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

        model.addAttribute("course", course);
        model.addAttribute("videos", videos);
        model.addAttribute("currentVideo", currentVideo);
        model.addAttribute("isAIChatExpanded", false); // Mặc định không mở rộng khung chat AI

        return "Video_Learning";
    }
} 