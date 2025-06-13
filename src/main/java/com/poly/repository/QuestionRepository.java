package com.poly.repository;

import com.poly.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query
import org.springframework.data.repository.query.Param; // Import Param
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Using @Query to explicitly define the JPQL query
    // 'q.course.ID_khoa_hoc' directly references the field in the entity
    @Query("SELECT q FROM Question q WHERE q.course.ID_khoa_hoc = :courseId")
    List<Question> findQuestionsByCourseId(@Param("courseId") Long courseId); // Method name can be anything now
}