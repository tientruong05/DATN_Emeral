package com.poly.service;

import com.poly.entity.Question;
import com.poly.repository.QuestionRepository;
import com.poly.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImp implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    @Override
    public Question findById(Long id) {
        return questionRepository.findById(id).orElse(null);
    }

    @Override
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public void delete(Long id) {
        questionRepository.deleteById(id);
    }

    @Override
    public List<Question> findByCourseID_khoa_hoc(Long courseId) {
        // CALL THE CORRECTED REPOSITORY METHOD HERE
        return questionRepository.findQuestionsByCourseId(courseId);
    }
}