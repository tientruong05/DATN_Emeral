package com.poly.dto;

public class CourseCertificateStatisticsDTO {
    private int rank;
    private String courseName;
    private long totalStudents;
    private long completedCertificates;
    private double completionRate;
    private String completionStatus;
    private long activeStudents; // Added based on your HTML table structure

    public CourseCertificateStatisticsDTO(int rank, String courseName, long totalStudents,
                                         long completedCertificates, double completionRate,
                                         String completionStatus, long activeStudents) {
        this.rank = rank;
        this.courseName = courseName;
        this.totalStudents = totalStudents;
        this.completedCertificates = completedCertificates;
        this.completionRate = completionRate;
        this.completionStatus = completionStatus;
        this.activeStudents = activeStudents;
    }

    // Getters and Setters (needed for Thymeleaf and data binding)
    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getCourseName() {
        return courseName;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public long getCompletedCertificates() {
        return completedCertificates;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public long getActiveStudents() {
        return activeStudents;
    }
}