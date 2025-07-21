package com.poly.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import com.poly.entity.Course;
// dto/CourseStatsDTO.java
public class CourseStatsDTO {
    private String courseName;
    private int totalStudents;
    private int inProgress;
    private int completed;

    public CourseStatsDTO() {}

    public CourseStatsDTO(String courseName, int totalStudents, int inProgress, int completed) {
        this.courseName = courseName;
        this.totalStudents = totalStudents;
        this.inProgress = inProgress;
        this.completed = completed;
    }

    // Getters và Setters
}
