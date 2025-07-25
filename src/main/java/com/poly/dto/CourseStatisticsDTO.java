package com.poly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatisticsDTO {
    private int rank;
    private String courseName;
    private long totalStudents;
    private long activeStudents;
    private long completedStudents;
    private double completionRate;
    private String popularityStatus; // e.g., "Hot", "Popular", "Normal"
}