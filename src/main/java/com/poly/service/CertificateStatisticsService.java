package com.poly.service;

import com.poly.dto.CertificateChartData;
import com.poly.dto.CertificateSummaryDTO;
import com.poly.dto.CourseCertificateStatisticsDTO;
import com.poly.entity.Enrollment;
import com.poly.repository.EnrollmentsRepository;
import com.poly.repository.CourseRepository;
import com.poly.entity.Course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class CertificateStatisticsService {

    @Autowired
    private EnrollmentsRepository enrollmentsRepository;

    @Autowired
    private CourseRepository courseRepository;

    // --- 1. Summary Statistics ---
    public CertificateSummaryDTO getCertificateSummary() {
        // Corrected method calls based on the updated repository
        long totalCertificatesIssued = enrollmentsRepository.countAllFinishedEnrollments(); // Counts all finished enrollments
        long totalActiveStudents = enrollmentsRepository.countDistinctActiveUsers(); // Counts distinct users with active enrollments
        long totalStudentsEverEnrolled = enrollmentsRepository.countDistinctUsers(); // Total unique students ever enrolled

        // "Học viên chờ cấp chứng chỉ" (Students pending certificate)
        // This is a common interpretation: students who have finished courses (distinct users)
        // but perhaps the certificates haven't been "issued" yet (if you had a separate flag for that).
        // If "totalCertificatesIssued" above refers to *enrollments* and not distinct users,
        // then `totalDistinctCompletedUsers - totalCertificatesIssued` (if a user only gets one certificate regardless of courses)
        // Or, more simply, it could be `totalEnrollmentsFinished - totalCertificatesIssued` if each finished enrollment results in a cert.

        // Let's use `countDistinctCompletedUsers` for the "completed" part if you mean unique individuals.
        // If totalCertificatesIssued is a count of *enrollments* that are finished and considered "issued",
        // then it makes sense to subtract.
        long distinctCompletedUsers = enrollmentsRepository.countDistinctCompletedUsers();
        // Assuming a certificate is issued *per finished enrollment*, then `totalCertificatesIssued` works.
        // If a certificate is *per user* regardless of how many courses they finish, you'd need a different tracking mechanism.
        // For simplicity now, let's assume `pendingCertificates` is based on "finished enrollments" vs. "actual certificates issued".
        // If `totalCertificatesIssued` above means `countAllFinishedEnrollments()`, and if every finished enrollment implies a certificate:
        // Then `pendingCertificates` might represent a backlog or an edge case.
        // Let's align it: `totalCertificatesIssued` is the count of finished enrollments.
        // If a separate "certificateIssued" boolean exists in Enrollment, you'd query for `countByFinishDateIsNotNullAndCertificateIssuedIsFalse`.
        // As you don't have that, we'll use a simplified version, but note this might need refinement based on your exact business rule.

        // Placeholder logic for pendingCertificates:
        // Assuming 'pending' means finished but not yet fully processed/marked.
        // This often requires a dedicated flag in the Enrollment entity (e.g., `boolean certificateProcessed`).
        // Without such a flag, it's hard to accurately calculate "pending" distinct from "issued".
        // Let's approximate: count of finished enrollments minus a hypothetical "actually issued" count (which you'd track).
        // For this example, I'll use 0 or a simple calculation, but mark it for real implementation.
        long pendingCertificates = 0; // Placeholder: Implement real logic if you add a 'certificateIssued' flag

        double completionRate = (totalStudentsEverEnrolled > 0) ?
                                ((double) distinctCompletedUsers / totalStudentsEverEnrolled) * 100 : 0.0;

        // Placeholder for previous month data (you'd need to fetch real historical data)
        long previousMonthCertificates = (long) (totalCertificatesIssued / 1.124);
        long previousMonthActiveStudents = (long) (totalActiveStudents / 1.089);
        double previousMonthCompletionRate = completionRate / 1.037;
        long previousMonthPendingCertificates = (long) (pendingCertificates / 0.979);

        // Calculate percentage change
        double certChange = calculatePercentageChange(totalCertificatesIssued, previousMonthCertificates);
        double activeChange = calculatePercentageChange(totalActiveStudents, previousMonthActiveStudents);
        double completionChange = calculatePercentageChange(completionRate, previousMonthCompletionRate);
        double pendingChange = calculatePercentageChange(pendingCertificates, previousMonthPendingCertificates);


        return new CertificateSummaryDTO(
            totalCertificatesIssued,
            certChange,
            totalActiveStudents,
            activeChange,
            completionRate,
            completionChange,
            pendingCertificates,
            pendingChange,
            totalStudentsEverEnrolled
        );
    }

    // --- 2. Chart Data ---
    public CertificateChartData getChartData(String timeRange) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;

        switch (timeRange) {
            case "7days":
                startDate = endDate.minusDays(7);
                break;
            case "30days":
                startDate = endDate.minusDays(30);
                break;
            case "3months":
                startDate = endDate.minusMonths(3);
                break;
            case "6months":
                startDate = endDate.minusMonths(6);
                break;
            case "1year":
                startDate = endDate.minusYears(1);
                break;
            default:
                startDate = endDate.minusMonths(1); // Default to 30 days if invalid
        }

        // --- Core Logic for Chart Data ---
        // Determine which type to pass based on the repository method's expectation
        // For countByFinishDateBetween, it expects LocalDate:
        LocalDate localStartDateForFinishDate = startDate.toLocalDate();
        LocalDate localEndDateForFinishDate = endDate.toLocalDate();

        // For countDistinctActiveUsersInRange and countDistinctUnfinishedUsersInRange,
        // they are now expecting LocalDateTime (as their queries include enrollmentDate)
        // So, we pass the original startDate and endDate (LocalDateTime)
        LocalDateTime localDateTimeStartDate = startDate; // Already LocalDateTime
        LocalDateTime localDateTimeEndDate = endDate;     // Already LocalDateTime


        // Use the correct types when calling the repository methods:
        long chart_completed = enrollmentsRepository.countByFinishDateBetween(localStartDateForFinishDate, localEndDateForFinishDate); // Pass LocalDate
        long chart_active = enrollmentsRepository.countDistinctActiveUsersInRange(localStartDateForFinishDate, localDateTimeEndDate); // Pass LocalDate, LocalDateTime
        long chart_unfinished = enrollmentsRepository.countDistinctUnfinishedUsersInRange(localDateTimeStartDate, localDateTimeEndDate); // Pass LocalDateTime


        return new CertificateChartData(
            List.of("Đã hoàn thành", "Đang học", "Chưa hoàn thành"),
            List.of((double) chart_completed, (double) chart_active, (double) chart_unfinished),
            List.of("#28a745", "#ffc107", "#dc3545")
        );
    }
    // --- 3. Table Data (by Course) ---
    public List<CourseCertificateStatisticsDTO> getCourseCertificateStatistics() {
        List<Course> courses = courseRepository.findAll();

        return courses.stream()
            .map(course -> {
                long totalEnrollments = enrollmentsRepository.countByCourse(course);
                long completedEnrollments = enrollmentsRepository.countByCourseAndFinishDateIsNotNull(course);
                long activeEnrollments = enrollmentsRepository.countByCourseAndStatusIsTrue(course);

                double completionRate = (totalEnrollments > 0) ?
                                        ((double) completedEnrollments / totalEnrollments) * 100 : 0.0;

                String completionStatus = "Trung bình";
                if (completionRate >= 85.0) {
                    completionStatus = "Xuất sắc";
                } else if (completionRate >= 70.0) {
                    completionStatus = "Giỏi";
                } else if (completionRate >= 50.0) {
                    completionStatus = "Khá";
                }

                return new CourseCertificateStatisticsDTO(
                    0, // Rank will be set after sorting
                    course.getTen_khoa_hoc(),
                    totalEnrollments,
                    completedEnrollments,
                    completionRate,
                    completionStatus,
                    activeEnrollments
                );
            })
            .sorted(Comparator.comparingLong(CourseCertificateStatisticsDTO::getTotalStudents).reversed()) // Sort by total students for ranking
            .collect(Collectors.toList());
    }

    // Helper for percentage change (from your previous code)
    private double calculatePercentageChange(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100.0;
    }

    private double calculatePercentageChange(double current, double previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100.0;
    }
}