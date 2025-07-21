package com.poly.controller;

import com.poly.dto.CertificateChartData;
import com.poly.dto.CertificateSummaryDTO;
import com.poly.dto.CourseCertificateStatisticsDTO;
import com.poly.dto.CourseStatisticsDTO;
import com.poly.dto.StudentStatisticsDTO;
import com.poly.service.CertificateStatisticsService;
import com.poly.service.EnrollmentService; // Use your EnrollmentService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/statistics")
public class StatisticsController {

    @Autowired
    private EnrollmentService enrollmentService; // Autowire your existing EnrollmentService

    @GetMapping("/students")
    public String showStudentStatistics(Model model) {
        // Summary Statistics
        long totalStudents = enrollmentService.getTotalStudents();
        long activeStudents = enrollmentService.getActiveStudents();
        long completedStudents = enrollmentService.getCompletedStudents();
        double avgStudentsPerCourse = enrollmentService.getAverageStudentsPerCourse();

        // **Important: For accurate percentage changes, you would need
        // historical data (e.g., from the previous month).
        // This example uses dummy values for demonstration.**
        long previousMonthTotalStudents = 2878; // Dummy value for demonstration
        long previousMonthActiveStudents = 1987; // Dummy value
        long previousMonthCompletedStudents = 946; // Dummy value
        double previousMonthAvgStudentsPerCourse = 174; // Dummy value

        StudentStatisticsDTO stats = new StudentStatisticsDTO(
                totalStudents,
                activeStudents,
                completedStudents,
                avgStudentsPerCourse,
                enrollmentService.calculatePercentageChange(totalStudents, previousMonthTotalStudents),
                enrollmentService.calculatePercentageChange(activeStudents, previousMonthActiveStudents),
                enrollmentService.calculatePercentageChange(completedStudents, previousMonthCompletedStudents),
                enrollmentService.calculatePercentageChange( (long) avgStudentsPerCourse, (long) previousMonthAvgStudentsPerCourse)
        );
        model.addAttribute("stats", stats);

        // Top Courses by Students
        List<CourseStatisticsDTO> topCourses = enrollmentService.getTopCoursesByStudents(7); // Get top 7 courses
        // Set rank after sorting
        for (int i = 0; i < topCourses.size(); i++) {
            topCourses.get(i).setRank(i + 1);
        }
        model.addAttribute("topCourses", topCourses);

        return "Statistic_Student"; // This will resolve to src/main/resources/templates/admin/Statistic_Student.html
    }

    // Endpoint for chart data (called via AJAX from frontend)
    @GetMapping("/students/chart-data")
    @ResponseBody // This tells Spring to return the data directly as JSON
    public Map<String, Object> getStudentChartData(@RequestParam(name = "status", defaultValue = "all") String statusFilter) {
        Map<String, Long> distribution = enrollmentService.getStudentDistributionByCourse(statusFilter);

        List<String> labels = new ArrayList<>(distribution.keySet());
        List<Long> data = new ArrayList<>(distribution.values());

        // Define colors (you might want to make this dynamic or fetch from a config)
        List<String> colors = List.of("#ff6b6b", "#4ecdc4", "#45b7d1", "#96ceb4", "#ffeaa7", "#dda0dd", "#98d8c8", "#ff9a8d", "#c2e9fb");
        // Ensure enough colors for all labels, or cycle through them
        List<String> assignedColors = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            assignedColors.add(colors.get(i % colors.size()));
        }

        return Map.of(
                "labels", labels,
                "data", data,
                "colors", assignedColors
        );
    }
    @Autowired
    private CertificateStatisticsService certificateStatisticsService;

    @GetMapping("/certificate")
    public String showCertificateStatistics(Model model) {
        // Fetch summary data
        CertificateSummaryDTO summary = certificateStatisticsService.getCertificateSummary();
        model.addAttribute("summary", summary);

        // Fetch table data
        List<CourseCertificateStatisticsDTO> courseStats = certificateStatisticsService.getCourseCertificateStatistics();
        // Set rank after sorting
        for (int i = 0; i < courseStats.size(); i++) {
            courseStats.get(i).setRank(i + 1);
        }
        model.addAttribute("courseStats", courseStats);

        return "Statistic_Certificate"; // This will map to src/main/resources/templates/admin/statistic_certificate.html
    }

    // API endpoint to fetch chart data (for JavaScript to call dynamically)
    @GetMapping("/certificate/chart-data")
    @ResponseBody // Important: return JSON directly
    public CertificateChartData getCertificateChartData(@RequestParam(name = "timeRange", defaultValue = "30days") String timeRange) {
        return certificateStatisticsService.getChartData(timeRange);
    }
}