package com.poly.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.poly.dto.QuizSubmissionDTO;
import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.Question;
import com.poly.entity.User;
import com.poly.service.CourseService;
import com.poly.service.EnrollmentService;
import com.poly.service.QuestionService;
import com.poly.service.VideoService;

import jakarta.servlet.http.HttpSession;

@Controller
public class QuizController {

    private static final Logger log = LoggerFactory.getLogger(QuizController.class);

    @Autowired
    private CourseService courseService;
    @Autowired
    private VideoService videoService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping({"/video-learning/quiz/{courseId}", "/quiz/{courseId}"})
    public String showQuiz(@PathVariable("courseId") Long courseId, Model model, HttpSession session) {
        log.info("Accessing quiz for courseId: {}", courseId);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            log.warn("User not logged in, redirecting to login");
            model.addAttribute("errorMessage", "Vui lòng đăng nhập để làm bài kiểm tra.");
            return "redirect:/Login_Signin";
        }

        if (courseId == null || courseId <= 0) {
            log.warn("Invalid courseId: {}, redirecting to index", courseId);
            model.addAttribute("errorMessage", "Khóa học không hợp lệ.");
            model.addAttribute("hasCompletedCourse", false); // Đảm bảo giá trị mặc định
            return "redirect:/index";
        }

        Course course = courseService.findById(courseId);
        if (course == null) {
            log.warn("Course not found for courseId: {}, redirecting to index", courseId);
            model.addAttribute("errorMessage", "Khóa học không tồn tại.");
            model.addAttribute("hasCompletedCourse", false); // Đảm bảo giá trị mặc định
            return "redirect:/index";
        }

        // Kiểm tra trạng thái hoàn thành khóa học
        boolean hasCompletedCourse = false;
        try {
            Enrollment enrollment = enrollmentService.findByUserAndCourse(user.getIdNguoiDung(), courseId);
            hasCompletedCourse = enrollment != null && enrollment.getFinishDate() != null;
            if (hasCompletedCourse) {
                log.info("User {} has already completed course {}, redirecting to video learning", user.getIdNguoiDung(), courseId);
                model.addAttribute("errorMessage", "Bạn đã hoàn thành khóa học này và không thể làm bài kiểm tra lại.");
                model.addAttribute("hasCompletedCourse", true);
                return "redirect:/video-learning/" + courseId;
            }
        } catch (Exception e) {
            log.error("Error checking enrollment for userId: {}, courseId: {}", user.getIdNguoiDung(), courseId, e);
            model.addAttribute("errorMessage", "Lỗi khi kiểm tra trạng thái khóa học.");
            model.addAttribute("hasCompletedCourse", false);
        }

        List<Question> questions = questionService.findByCourseID_khoa_hoc(courseId);
        model.addAttribute("course", course);
        if (questions != null && !questions.isEmpty()) {
            model.addAttribute("questions", questions);
            model.addAttribute("currentQuestion", questions.get(0));
        } else {
            log.warn("No questions found for courseId: {}", courseId);
            model.addAttribute("errorMessage", "Chưa có câu hỏi cho khóa học này.");
            model.addAttribute("questions", List.of());
            model.addAttribute("currentQuestion", null);
        }

        int totalTimeInSeconds = (course.getDiem_dat() != null && course.getDiem_dat() > 0) ? (int) (course.getDiem_dat() * 60) : 600;
        model.addAttribute("totalTimeInSeconds", totalTimeInSeconds);
        model.addAttribute("user", user);
        model.addAttribute("hasCompletedCourse", hasCompletedCourse); // Đảm bảo luôn truyền giá trị

        return "Quiz";
    }

    @PostMapping("/submit-quiz")
    @ResponseBody
    public Map<String, Object> submitQuiz(@RequestBody QuizSubmissionDTO dto, HttpSession session) {
        log.info("Received submit-quiz request at {}: DTO - courseId: {}, answers: {}, timeLeft: {}, full DTO: {}", 
                 new java.util.Date(), dto != null ? dto.getCourseId() : null, 
                 dto != null ? dto.getAnswers() : null, 
                 dto != null ? dto.getTimeLeft() : null, dto);

        User user = (User) session.getAttribute("user");
        if (user == null) {
            log.error("User not logged in for submit-quiz");
            throw new IllegalArgumentException("Please log in to submit the quiz");
        }

        if (dto == null) {
            log.error("DTO is null in submit-quiz request");
            throw new IllegalArgumentException("Request body cannot be null");
        }

        Long courseId = dto.getCourseId();
        if (courseId == null) {
            log.warn("CourseId is null, using default value 1 for testing. Full DTO: {}", dto);
            courseId = 1L;
        } else if (courseId <= 0) {
            log.error("CourseId is invalid (value: {}) in submit-quiz request. Full DTO: {}", courseId, dto);
            throw new IllegalArgumentException("Course ID cannot be zero or negative");
        }

        log.debug("Fetching course for courseId: {}", courseId);
        Course course = courseService.findById(courseId);
        if (course == null) {
            log.error("Course not found for courseId: {}", courseId);
            throw new IllegalArgumentException("Course not found");
        }

        log.debug("Fetching questions for courseId: {}", courseId);
        List<Question> questions = questionService.findByCourseID_khoa_hoc(courseId);
        if (questions == null || questions.isEmpty()) {
            log.error("No questions found for courseId: {}", courseId);
            throw new IllegalArgumentException("No questions available");
        }

        Map<String, String> answers = dto.getAnswers();
        int timeLeft = dto.getTimeLeft();

        double score = 0;
        int totalQuestions = questions.size();
        log.debug("Calculating score with {} answers for {} questions", answers != null ? answers.size() : 0, totalQuestions);

        if (answers != null) {
            for (int i = 0; i < totalQuestions; i++) {
                Question q = questions.get(i);
                String userAnswer = answers.get(String.valueOf(i));
                log.debug("Question {} (ID_cau_hoi: {}): userAnswer = {}, dap_an_dung = {}", 
                          i, q.getID_cau_hoi(), userAnswer, q.getDap_an_dung());
                if (userAnswer != null) {
                    String userAnswerContent = null;
                    switch (userAnswer.toUpperCase()) {
                        case "A": userAnswerContent = q.getDap_an_a(); break;
                        case "B": userAnswerContent = q.getDap_an_b(); break;
                        case "C": userAnswerContent = q.getDap_an_c(); break;
                        case "D": userAnswerContent = q.getDap_an_d(); break;
                    }

                    log.debug("Answer options A={}, B={}, C={}, D={}", 
                        q.getDap_an_a(), q.getDap_an_b(), q.getDap_an_c(), q.getDap_an_d());
                    log.debug("Comparing userAnswerContent = '{}' vs dap_an_dung = '{}'", 
                        userAnswerContent, q.getDap_an_dung());

                    if (userAnswerContent != null && userAnswerContent.trim().equalsIgnoreCase(q.getDap_an_dung().trim())) {
                        score++;
                        log.debug("Match found for question {} (ID_cau_hoi: {})", i, q.getID_cau_hoi());
                    }
                }
            }
        }
     
        int correctCount = 0;
        int answeredCount = 0;

        if (answers != null) {
            for (int i = 0; i < totalQuestions; i++) {
                Question q = questions.get(i);
                String userAnswer = answers.get(String.valueOf(i));

                if (userAnswer != null) {
                    answeredCount++; // <== Đếm số câu có chọn
                    String userAnswerContent = null;
                    switch (userAnswer.toUpperCase()) {
                        case "A": userAnswerContent = q.getDap_an_a(); break;
                        case "B": userAnswerContent = q.getDap_an_b(); break;
                        case "C": userAnswerContent = q.getDap_an_c(); break;
                        case "D": userAnswerContent = q.getDap_an_d(); break;
                    }

                    if (userAnswerContent != null && userAnswerContent.trim().equalsIgnoreCase(q.getDap_an_dung().trim())) {
                        correctCount++;
                    }
                }
            }
        }

        // Nếu không trả lời câu nào → score = 0
      
        if (answeredCount == 0) {
            score = 0;
            log.info("User submitted blank quiz (0 answers). Score set to 0.");
        } else {
            score = ((double) correctCount / totalQuestions) * 100;
        }

       
        log.debug("Calculated score: {}", score);
        

        // Di chuyển dòng này ra đây
        double score10 = (score / 100.0) * 10.0;
        // Lưu kết quả vào enrollment
        if (user != null) {
            log.debug("Fetching or creating enrollment for userId: {}, courseId: {}", user.getIdNguoiDung(), courseId);
            Enrollment enrollment = enrollmentService.findByUserAndCourse(user.getIdNguoiDung(), courseId);
            if (enrollment == null) {
                enrollment = new Enrollment();
                enrollment.setUser(user);
                enrollment.setCourse(course);
                enrollment.setPrice(course.getGia_tien());
                enrollment.setEnrollmentDate(LocalDateTime.now());
            }
            enrollment.setDiem(score);
           
            if (score10 >= course.getDiem_dat()) {
                enrollment.setFinishDate(LocalDate.now());
            }


            enrollmentService.save(enrollment);
            log.info("Saved enrollment for user {} with score {}", user.getIdNguoiDung(), score);
        } else {
            log.warn("No user in session after initial check");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("score", score);
        response.put("pass", score10 >= course.getDiem_dat());

        response.put("totalQuestions", totalQuestions);
        response.put("correctAnswers", (int) score);
        log.info("Returning response: {}", response);
        return response;
    }
}
