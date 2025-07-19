package com.poly.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record ProgressUpdateDTO(
    @NotNull(message = "User ID không được để trống")
    Long userId,
    
    @NotNull(message = "Course ID không được để trống")
    Long courseId,
    
    @NotNull(message = "Video ID không được để trống")
    Long videoId,
    
    @Min(value = 0, message = "Progress phải từ 0-100")
    @Max(value = 100, message = "Progress phải từ 0-100")
    Float progress
) {
    public ProgressUpdateDTO {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống");
        }
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID không được để trống");
        }
        if (videoId == null) {
            throw new IllegalArgumentException("Video ID không được để trống");
        }
        if (progress == null || progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress phải từ 0-100");
        }
    }
} 