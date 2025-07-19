package com.poly.dto;

public record CourseProgressDTO(
    Long courseId,
    String courseName,
    Integer totalVideos,
    Integer completedVideos,
    Float progress,
    Boolean isCompleted
) {
    public CourseProgressDTO {
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID không được để trống");
        }
        if (courseName == null || courseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Course name không được để trống");
        }
        if (totalVideos == null || totalVideos < 0) {
            throw new IllegalArgumentException("Total videos phải >= 0");
        }
        if (completedVideos == null || completedVideos < 0) {
            throw new IllegalArgumentException("Completed videos phải >= 0");
        }
        if (progress == null || progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress phải từ 0-100");
        }
        if (isCompleted == null) {
            throw new IllegalArgumentException("Is completed không được để trống");
        }
    }
} 