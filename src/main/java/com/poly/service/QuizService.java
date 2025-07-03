package com.poly.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.dto.QuizSubmissionDTO;
import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.Question;
import com.poly.entity.User;

@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    @Autowired
    private CourseService courseService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private EnrollmentService enrollmentService;

    /**
     * Lấy danh sách câu hỏi cho một khóa học
     * @param courseId ID của khóa học
     * @return Danh sách câu hỏi
     */
    public List<Question> getQuestionsByCourseId(Long courseId) {
        log.debug("Fetching questions for courseId: {}", courseId);
        List<Question> questions = questionService.findByCourseID_khoa_hoc(courseId);
        if (questions == null || questions.isEmpty()) {
            log.warn("No questions found for courseId: {}", courseId);
        }
        return questions;
    }

    /**
     * Tính điểm và lưu kết quả bài kiểm tra
     * @param dto Dữ liệu nộp bài từ client
     * @param user Người dùng hiện tại
     * @return Map chứa kết quả (score, pass, totalQuestions, correctAnswers)
     */
    public Map<String, Object> calculateAndSaveQuizResult(QuizSubmissionDTO dto, User user) {
        if (dto == null) {
            log.error("DTO is null in calculateAndSaveQuizResult");
            throw new IllegalArgumentException("Request body cannot be null");
        }

        Long courseId = dto.getCourseId() != null ? dto.getCourseId() : 1L; // Default to 1 if null
        if (courseId <= 0) {
            log.error("CourseId is invalid (value: {})", courseId);
            throw new IllegalArgumentException("Course ID cannot be zero or negative");
        }

        Course course = courseService.findById(courseId);
        if (course == null) {
            log.error("Course not found for courseId: {}", courseId);
            throw new IllegalArgumentException("Course not found");
        }

        List<Question> questions = getQuestionsByCourseId(courseId);
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
                String userAnswer = answers.get(String.valueOf(i)); // "A", "B", "C", hoặc "D"
                log.debug("Question {} (ID_cau_hoi: {}): userAnswer = {}, dap_an_dung = {}", i, q.getID_cau_hoi(), userAnswer, q.getDap_an_dung());

                String correctAnswer = mapToCorrectAnswer(q, q.getDap_an_dung().trim().toUpperCase());
                if (userAnswer != null && userAnswer.trim().equalsIgnoreCase(correctAnswer.trim())) {
                    score++;
                    log.debug("Match found for question {} (ID_cau_hoi: {})", i, q.getID_cau_hoi());
                }
            }
        }
        score = (score / totalQuestions) * 100;
        log.debug("Calculated score: {}", score);

        // Lưu kết quả vào enrollment
        if (user != null) {
            Enrollment enrollment = enrollmentService.findByUserAndCourse(user.getIdNguoiDung(), courseId);
            if (enrollment == null) {
                enrollment = new Enrollment();
                enrollment.setUser(user);
                enrollment.setCourse(course);
                enrollment.setPrice(course.getGia_tien());
                enrollment.setEnrollmentDate(LocalDateTime.now());
            }
            enrollment.setDiem(score);
            if (score >= course.getDiem_dat()) {
                enrollment.setFinishDate(LocalDate.now());
            }
            enrollmentService.save(enrollment);
            log.info("Saved enrollment for user {} with score {}", user.getIdNguoiDung(), score);
        } else {
            log.warn("No user provided for saving enrollment");
        }

        // Chuẩn bị phản hồi
        Map<String, Object> response = new HashMap<>();
        response.put("score", score);
        response.put("pass", score >= course.getDiem_dat());
        response.put("totalQuestions", totalQuestions);
        response.put("correctAnswers", (int) score);
        log.info("Returning response: {}", response);
        return response;
    }

    /**
     * Ánh xạ đáp án đúng (A, B, C, D) với giá trị thực từ câu hỏi
     * @param question Câu hỏi
     * @param answerKey Đáp án đúng (A, B, C, D)
     * @return Giá trị thực của đáp án
     */
    private String mapToCorrectAnswer(Question question, String answerKey) {
        switch (answerKey) {
            case "A": return question.getDap_an_a();
            case "B": return question.getDap_an_b();
            case "C": return question.getDap_an_c();
            case "D": return question.getDap_an_d();
            default: return question.getDap_an_dung(); // Fallback
        }
    }
}