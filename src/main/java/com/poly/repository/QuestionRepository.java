package com.poly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
//    List<Question> findByCourseId(Long ID_khoa_hoc);
}