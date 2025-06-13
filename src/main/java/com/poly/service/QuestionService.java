package com.poly.service;

import com.poly.entity.Question;
import java.util.List;

public interface QuestionService {
    List<Question> findAll();
    Question findById(Long id);
    Question save(Question question);
    void delete(Long id);
    List<Question> findByCourseID_khoa_hoc(Long courseId); // Method to fetch questions for a specific course
}