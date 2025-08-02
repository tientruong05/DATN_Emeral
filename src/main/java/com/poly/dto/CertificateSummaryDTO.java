package com.poly.dto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CertificateSummaryDTO {
    private long totalCertificatesIssued;
    private double certChangePercentage;
    private long totalActiveStudents;
    private double activeChangePercentage;
    private double completionRate;
    private double completionChangePercentage;
    private long studentsPendingCertificate;
    private double pendingChangePercentage;
    // ADD THIS FIELD:
    private long totalStudentsEverEnrolled; // Add this field

    public CertificateSummaryDTO(long totalCertificatesIssued, double certChangePercentage,
                                 long totalActiveStudents, double activeChangePercentage,
                                 double completionRate, double completionChangePercentage,
                                 long studentsPendingCertificate, double pendingChangePercentage,
                                 long totalStudentsEverEnrolled) { // And add to constructor
        this.totalCertificatesIssued = totalCertificatesIssued;
        this.certChangePercentage = certChangePercentage;
        this.totalActiveStudents = totalActiveStudents;
        this.activeChangePercentage = activeChangePercentage;
        this.completionRate = completionRate;
        this.completionChangePercentage = completionChangePercentage;
        this.studentsPendingCertificate = studentsPendingCertificate;
        this.pendingChangePercentage = pendingChangePercentage;
        this.totalStudentsEverEnrolled = totalStudentsEverEnrolled; // Assign in constructor
    }

    
}