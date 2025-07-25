package com.poly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatisticsDTO {
    private long totalStudents;
    private long activeStudents;
    private long completedStudents;
    private double avgStudentsPerCourse;
    private double totalStudentsChangePercentage;
    private double activeStudentsChangePercentage;
    private double completedStudentsChangePercentage;
    private double avgStudentsPerCourseChangePercentage;
}