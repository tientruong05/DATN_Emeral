package com.poly.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.poly.entity.Course;
import com.poly.entity.Enrollment;
import com.poly.entity.User;
import com.poly.repository.CourseRepository;
import com.poly.repository.UserRepository;
import com.poly.service.CourseService;
import com.poly.service.EnrollmentService;
import com.poly.service.MailService;

import jakarta.servlet.http.HttpSession;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Controller
public class CertificateController {

    private static final Logger logger = LoggerFactory.getLogger(CertificateController.class);

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

            // Định dạng nội dung email với thông báo rõ ràng về tệp PDF
            double score10 = enrollment.getDiem() / 10.0;
            String content = """
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;">
                        <h2 style="color: #2c3e50; text-align: center;">Chúc mừng bạn đã hoàn thành khóa học!</h2>
                        <p>Kính gửi <strong>%s</strong>,</p>
                        <p>Chúng tôi xin chúc mừng bạn đã hoàn thành xuất sắc khóa học <strong>"%s"</strong> 
                        với số điểm <strong>%.0f/10</strong>.</p>
                        <p><strong>Chứng chỉ của bạn đã được đính kèm trong email này dưới dạng tệp PDF (certificate.pdf).</strong> 
                        Vui lòng kiểm tra phần đính kèm để xem và lưu chứng chỉ của bạn.</p>
                      
                        <p>Chúng tôi hy vọng bạn sẽ tiếp tục khám phá các khóa học khác để nâng cao kiến thức và kỹ năng của mình!</p>
                        <p>Trân trọng,<br><strong>Đội ngũ Emeral</strong></p>
                    </div>
                </body>
                </html>
                """.formatted(user.getHoTen(), course.getTen_khoa_hoc(), score10, courseId, userId);

            // Tạo PDF để gửi kèm mail
            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("course", course);
            context.setVariable("enrollment", enrollment);
            String htmlContent = templateEngine.process("CertificatePDF", context);

            byte[] pdfBytes = null;
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.useFont(() -> {
                    InputStream fontStream = getClass().getResourceAsStream("/font/Pattaya-Regular.ttf");
                    if (fontStream == null) {
                        logger.error("Font Pattaya-Regular.ttf not found");
                        throw new IllegalStateException("Font Pattaya-Regular.ttf not found");
                    }
                    return fontStream;
                }, "Pattaya");
                builder.useFont(() -> {
                    InputStream fontStream = getClass().getResourceAsStream("/font/MeaCulpa-Regular.ttf");
                    if (fontStream == null) {
                        logger.error("Font MeaCulpa-Regular.ttf not found");
                        throw new IllegalStateException("Font MeaCulpa-Regular.ttf not found");
                    }
                    return fontStream;
                }, "Mea Culpa");
                builder.useFont(() -> {
                    InputStream fontStream = getClass().getResourceAsStream("/font/Roboto-Regular.ttf");
                    if (fontStream == null) {
                        logger.error("Font Roboto-Regular.ttf not found");
                        throw new IllegalStateException("Font Roboto-Regular.ttf not found");
                    }
                    return fontStream;
                }, "Roboto");
                builder.useFont(() -> {
                    InputStream fontStream = getClass().getResourceAsStream("/font/PlayfairDisplay-VariableFont_wght.ttf");
                    if (fontStream == null) {
                        logger.error("Font PlayfairDisplay-VariableFont_wght.ttf not found");
                        throw new IllegalStateException("Font PlayfairDisplay-VariableFont_wght.ttf not found");
                    }
                    return fontStream;
                }, "Playfair Display");
                builder.withHtmlContent(htmlContent, null);
                builder.toStream(outputStream);
                builder.run();
                pdfBytes = outputStream.toByteArray();
            } catch (Exception e) {
                logger.error("Failed to generate PDF for userId: {} and courseId: {}", userId, courseId, e);
            }

            if (pdfBytes != null) {
                mailService.sendCertificateMailWithAttachment(user.getEmail(), subject, content, pdfBytes,
                        "certificate_" + userId + "_" + courseId + ".pdf");
            } else {
                logger.warn("PDF generation failed, sending email without attachment");
                mailService.sendCertificateMail(user.getEmail(), subject, content);
            }
        } else {
            logger.warn("Invalid data for certificate: userId={}, courseId={}, enrollment={}", userId, courseId, enrollment);
        }
        return "Certificate";
    }

    @GetMapping("/certificate/download/{courseId}/{userId}")
    public ResponseEntity<ByteArrayResource> downloadCertificate(@PathVariable Long courseId,
            @PathVariable Long userId) {
        try {
            Course course = courseService.findById(courseId);
            if (course == null) {
                logger.error("Course not found for ID: {}", courseId);
                return ResponseEntity.badRequest().body(null);
            }

            Enrollment enrollment = enrollmentService.findByUserAndCourse(userId, courseId);
            if (enrollment == null) {
                logger.error("Enrollment not found for userId: {} and courseId: {}", userId, courseId);
                return ResponseEntity.badRequest().body(null);
            }

            User user = enrollment.getUser();
            if (user == null) {
                logger.error("User not found for enrollment with userId: {}", userId);
                return ResponseEntity.badRequest().body(null);
            }

            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("course", course);
            context.setVariable("enrollment", enrollment);

            String htmlContent = templateEngine.process("CertificatePDF", context);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.useFont(() -> {
                    InputStream fontStream = getClass().getResourceAsStream("/font/Merriweather-VariableFont_opsz,wdth,wght.ttf");
                    if (fontStream == null) {
                        logger.error("Font Merriweather-VariableFont_opsz,wdth,wght.ttf not found");
                        throw new IllegalStateException("Font Merriweather-VariableFont_opsz,wdth,wght.ttf not found");
                    }
                    return fontStream;
                }, "Merri");
                builder.useFont(() -> {
                    InputStream fontStream = getClass().getResourceAsStream("/font/MeaCulpa-Regular.ttf");
                    if (fontStream == null) {
                        logger.error("Font MeaCulpa-Regular.ttf not found");
                        throw new IllegalStateException("Font MeaCulpa-Regular.ttf not found");
                    }
                    return fontStream;
                }, "Mea Culpa");
                builder.useFont(() -> {
                    InputStream fontStream = getClass().getResourceAsStream("/font/Roboto-Regular.ttf");
                    if (fontStream == null) {
                        logger.error("Font Roboto-Regular.ttf not found");
                        throw new IllegalStateException("Font Roboto-Regular.ttf not found");
                    }
                    return fontStream;
                }, "Roboto");
                builder.useFont(() -> {
                    InputStream fontStream = getClass().getResourceAsStream("/font/PlayfairDisplay-VariableFont_wght.ttf");
                    if (fontStream == null) {
                        logger.error("Font PlayfairDisplay-VariableFont_wght.ttf not found");
                        throw new IllegalStateException("Font PlayfairDisplay-VariableFont_wght.ttf not found");
                    }
                    return fontStream;
                }, "Playfair Display");
                String baseUrl = getClass().getResource("/static/upload/").toExternalForm();
                builder.withHtmlContent(htmlContent, baseUrl);
//                builder.withHtmlContent(htmlContent, null);
                builder.toStream(outputStream);
                builder.run();

                byte[] pdfBytes = outputStream.toByteArray();

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate_" + userId + "_" + courseId + ".pdf");

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(new ByteArrayResource(pdfBytes));
            }
        } catch (Exception e) {
            logger.error("Failed to generate certificate for userId: {} and courseId: {}", userId, courseId, e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
}