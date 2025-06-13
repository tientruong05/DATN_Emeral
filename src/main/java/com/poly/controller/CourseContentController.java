package com.poly.controller;

import com.poly.entity.Course;
import com.poly.entity.Video; // Import Video entity
import com.poly.entity.Question; // Import Question entity
import com.poly.repository.CategoryRepository;
import com.poly.service.CourseService;
import com.poly.service.VideoService; // Import VideoService
import com.poly.service.QuestionService; // Import QuestionService

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // For @ModelAttribute
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // For @PostMapping
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // For RedirectAttributes

import java.util.List; // For List type if not already imported

@Controller
@RequestMapping("/course-content") // Base path for this controller
public class CourseContentController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private VideoService videoService; // Autowire VideoService

    @Autowired
    private QuestionService questionService; // Autowire QuestionService


    // --- Course Content Page Display ---
    @GetMapping("/{courseId}") // Maps to /course-content/{courseId}
    public String showCourseContent(@PathVariable("courseId") Long courseId, Model model) {
        Course course = courseService.findById(courseId);

        if (course == null) {
            // Handle case where course is not found, e.g., redirect to an error page
            return "redirect:/Crud_Course?error=courseNotFound";
        }

        model.addAttribute("course", course);
        // Add actual videos and questions related to this course
        model.addAttribute("videos", videoService.findByCourseID_khoa_hoc(courseId));
        model.addAttribute("questions", questionService.findByCourseID_khoa_hoc(courseId));

        // Add empty video/question objects for the "add" modals to bind to
        model.addAttribute("newVideo", new Video());
        model.addAttribute("newQuestion", new Question());

        return "course_content"; // This corresponds to your HTML file: course_content.html
    }

    // --- Video CRUD Operations ---

    // Method to handle adding a new video
    @PostMapping("/{courseId}/videos/add")
    public String addVideo(@PathVariable("courseId") Long courseId,
                           @ModelAttribute Video video,
                           RedirectAttributes redirectAttributes) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/Crud_Course";
        }
        video.setCourse(course); // Link the video to the course
        videoService.save(video);
        redirectAttributes.addFlashAttribute("successMessage", "Video added successfully!");
        return "redirect:/course-content/" + courseId + "#video-tab"; // Redirect back to video tab
    }

    // Method to handle updating an existing video
    @PostMapping("/{courseId}/videos/update")
    public String updateVideo(@PathVariable("courseId") Long courseId,
                              @ModelAttribute Video video,
                              RedirectAttributes redirectAttributes) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/Crud_Course";
        }
        // Basic check: Ensure the video exists and belongs to the correct course
        // For more robust validation, you might fetch and compare more properties
        Video existingVideo = videoService.findById(video.getID_video());
        if (existingVideo == null || !existingVideo.getCourse().getID_khoa_hoc().equals(courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Video not found or does not belong to this course.");
            return "redirect:/course-content/" + courseId + "#video-tab";
        }

        video.setCourse(course); // Ensure the course relationship is maintained
        videoService.save(video);
        redirectAttributes.addFlashAttribute("successMessage", "Video updated successfully!");
        return "redirect:/course-content/" + courseId + "#video-tab"; // Redirect back to video tab
    }

    // Method to handle deleting a video
    @GetMapping("/{courseId}/videos/delete/{videoId}")
    public String deleteVideo(@PathVariable("courseId") Long courseId,
                              @PathVariable("videoId") Long videoId,
                              RedirectAttributes redirectAttributes) {
        // Optional: Check if video belongs to course before deleting for security/validation
        Video videoToDelete = videoService.findById(videoId);
        if (videoToDelete == null || !videoToDelete.getCourse().getID_khoa_hoc().equals(courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Video not found or does not belong to this course.");
            return "redirect:/course-content/" + courseId + "#video-tab";
        }

        videoService.delete(videoId);
        redirectAttributes.addFlashAttribute("successMessage", "Video deleted successfully!");
        return "redirect:/course-content/" + courseId + "#video-tab"; // Redirect back to video tab
    }

    // --- Question CRUD Operations ---

    // Method to handle adding a new question
    @PostMapping("/{courseId}/questions/add")
    public String addQuestion(@PathVariable("courseId") Long courseId,
                              @ModelAttribute Question question,
                              RedirectAttributes redirectAttributes) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/Crud_Course";
        }
        String correctAnswer = question.getDap_an_dung();
        String answerA = question.getDap_an_a();
        String answerB = question.getDap_an_b();
        String answerC = question.getDap_an_c();
        String answerD = question.getDap_an_d();

        // Check if correct answer matches any of the options
        if (correctAnswer == null ||
            (!correctAnswer.equals(answerA) &&
             !correctAnswer.equals(answerB) &&
             !correctAnswer.equals(answerC) &&
             !correctAnswer.equals(answerD))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đáp án đúng phải trùng khớp với một trong các đáp án A, B, C, D.");
            // Keep the entered data so user doesn't lose it (optional, but good UX)
            redirectAttributes.addFlashAttribute("newQuestion", question);
            return "redirect:/course-content/" + courseId + "#quiz-tab";
        }
        question.setCourse(course); // Link the question to the course
        questionService.save(question);
        redirectAttributes.addFlashAttribute("successMessage", "Question added successfully!");
        return "redirect:/course-content/" + courseId + "#quiz-tab"; // Redirect back to quiz tab
    }

    // Method to handle updating an existing question
    @PostMapping("/{courseId}/questions/update")
    public String updateQuestion(@PathVariable("courseId") Long courseId,
                                 @ModelAttribute Question question,
                                 RedirectAttributes redirectAttributes) {
        Course course = courseService.findById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Course not found.");
            return "redirect:/Crud_Course";
        }
        // Basic check: Ensure the question exists and belongs to the correct course
        Question existingQuestion = questionService.findById(question.getID_cau_hoi());
        if (existingQuestion == null || !existingQuestion.getCourse().getID_khoa_hoc().equals(courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Question not found or does not belong to this course.");
            return "redirect:/course-content/" + courseId + "#quiz-tab";
        }
        String correctAnswer = question.getDap_an_dung();
        String answerA = question.getDap_an_a();
        String answerB = question.getDap_an_b();
        String answerC = question.getDap_an_c();
        String answerD = question.getDap_an_d();

        // Check if correct answer matches any of the options
        if (correctAnswer == null ||
            (!correctAnswer.equals(answerA) &&
             !correctAnswer.equals(answerB) &&
             !correctAnswer.equals(answerC) &&
             !correctAnswer.equals(answerD))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đáp án đúng phải trùng khớp với một trong các đáp án A, B, C, D.");
            // Keep the entered data so user doesn't lose it (optional, but good UX)
            redirectAttributes.addFlashAttribute("questionToUpdate", question); // Assuming your update form uses 'questionToUpdate'
            return "redirect:/course-content/" + courseId + "#quiz-tab";
        }
        question.setCourse(course); // Ensure the course relationship is maintained
        questionService.save(question);
        redirectAttributes.addFlashAttribute("successMessage", "Question updated successfully!");
        return "redirect:/course-content/" + courseId + "#quiz-tab"; // Redirect back to quiz tab
    }

    // Method to handle deleting a question
    @GetMapping("/{courseId}/questions/delete/{questionId}")
    public String deleteQuestion(@PathVariable("courseId") Long courseId,
                                 @PathVariable("questionId") Long questionId,
                                 RedirectAttributes redirectAttributes) {
        // Optional: Check if question belongs to course before deleting
        Question questionToDelete = questionService.findById(questionId);
        if (questionToDelete == null || !questionToDelete.getCourse().getID_khoa_hoc().equals(courseId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Question not found or does not belong to this course.");
            return "redirect:/course-content/" + courseId + "#quiz-tab";
        }

        questionService.delete(questionId);
        redirectAttributes.addFlashAttribute("successMessage", "Question deleted successfully!");
        return "redirect:/course-content/" + courseId + "#quiz-tab"; // Redirect back to quiz tab
    }
}