package com.poly.controller;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.User;
import com.poly.repository.CourseRepository;
import com.poly.repository.UserRepository;
import com.poly.service.CourseService;
import com.poly.service.EnrollmentService;
import com.poly.service.MailService;

import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Controller
public class CertificateController {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MailService mailService;

    @GetMapping("/certificate/{courseId}/{userId}")
    public String showCertificate(@PathVariable Long courseId, @PathVariable Long userId, Model model) {
        User user = userRepo.findById(userId).orElse(null);
        Course course = courseRepo.findById(courseId).orElse(null);
        Enrollment enrollment = enrollmentService.findByUserAndCourse(userId, courseId);

        model.addAttribute("user", user);
        model.addAttribute("course", course);
        model.addAttribute("enrollment", enrollment);

        if (user != null && course != null && enrollment != null && enrollment.getDiem() != null
                && enrollment.getDiem() >= course.getDiem_dat()) {

            String subject = "Chúc mừng bạn nhận được chứng chỉ khóa học " + course.getTen_khoa_hoc();
            double score10 = enrollment.getDiem() / 10.0;
            String content = "Xin chúc mừng " + user.getHoTen() + " đã hoàn thành khóa học \""
                    + course.getTen_khoa_hoc() + "\" với số điểm " + String.format("%.1f", score10)
                    + " trên thang điểm 10.\nBạn có thể tải chứng chỉ tại hệ thống.";

            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("course", course);
            context.setVariable("enrollment", enrollment);

            String htmlContent = templateEngine.process("CertificatePDF", context);

            byte[] pdfBytes = null;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.useFont(() -> getClass().getResourceAsStream("/font/Pattaya-Regular.ttf"), "Pattaya");
                builder.useFont(() -> getClass().getResourceAsStream("/font/MeaCulpa-Regular.ttf"), "Mea Culpa");
                builder.useFont(() -> getClass().getResourceAsStream("/font/Roboto-Regular.ttf"), "Roboto");
                builder.useFont(() -> getClass().getResourceAsStream("/font/PlayfairDisplay-VariableFont_wght.ttf"), "Playfair Display");

                // baseURL: nơi chứa hình ảnh "certificate.jpg"
                builder.withHtmlContent(htmlContent, getClass().getResource("/static/upload/").toString());
                builder.toStream(outputStream);
                builder.run();

                pdfBytes = outputStream.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (pdfBytes != null) {
                mailService.sendCertificateMailWithAttachment(user.getEmail(), subject, content, pdfBytes,
                        "certificate.pdf");
            } else {
                mailService.sendCertificateMail(user.getEmail(), subject, content);
            }
        }

        return "Certificate";
    }

    @GetMapping("/certificate/download/{courseId}/{userId}")
    public ResponseEntity<ByteArrayResource> downloadCertificate(@PathVariable Long courseId,
            @PathVariable Long userId) {

        Course course = courseService.findById(courseId);
        Enrollment enrollment = enrollmentService.findByUserAndCourse(userId, courseId);
        User user = enrollment.getUser();

        Context context = new Context(Locale.getDefault());
        context.setVariable("user", user);
        context.setVariable("course", course);
        context.setVariable("enrollment", enrollment);

        String htmlContent = templateEngine.process("CertificatePDF", context);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            builder.useFont(() -> getClass().getResourceAsStream("/font/Merriweather-VariableFont_opsz,wdth,wght.ttf"), "Merri");
            builder.useFont(() -> getClass().getResourceAsStream("/font/MeaCulpa-Regular.ttf"), "Mea Culpa");
            builder.useFont(() -> getClass().getResourceAsStream("/font/Roboto-VariableFont_wdth,wght.ttf"), "Roboto");
            builder.useFont(() -> getClass().getResourceAsStream("/font/PlayfairDisplay-VariableFont_wght.ttf"), "Playfair Display");

            String baseUrl = getClass().getResource("/static/upload/").toExternalForm();
            builder.withHtmlContent(htmlContent, baseUrl);
            
//            builder.withHtmlContent(htmlContent, getClass().getResource("/static/upload/").toString());
            builder.toStream(outputStream);
            builder.run();

            byte[] pdfBytes = outputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new ByteArrayResource(pdfBytes));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
