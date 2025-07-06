//package com.poly.config;
//
////import com.github.danielgvozden.html2image.Html2Image;
//import com.itextpdf.text.Document;
//import com.itextpdf.text.Image;
//import com.itextpdf.text.PageSize;
//import com.itextpdf.text.pdf.PdfWriter;
//import org.thymeleaf.context.Context;
//import org.thymeleaf.spring6.SpringTemplateEngine;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//
//public class HtmlToImage {
//
//    public static byte[] renderHtmlToPdfImage(String templateName, Context context, SpringTemplateEngine templateEngine) throws Exception {
//        // 1. Render HTML từ Thymeleaf
//        String html = templateEngine.process(templateName, context);
//
//        // 2. Convert HTML to BufferedImage
//        BufferedImage image = Html2Image.fromHtml(html).asBufferedImage();
//
//        // 3. Chuyển BufferedImage thành PDF
//        ByteArrayOutputStream pdfBaos = new ByteArrayOutputStream();
//        Document document = new Document(PageSize.A4.rotate(), 0, 0, 0, 0);
//        PdfWriter.getInstance(document, pdfBaos);
//        document.open();
//
//        // Chèn ảnh PNG vào PDF
//        Image pdfImage = Image.getInstance(toByteArray(image));
//        pdfImage.scaleToFit(PageSize.A4.getHeight(), PageSize.A4.getWidth());
//        pdfImage.setAlignment(Image.ALIGN_CENTER);
//        document.add(pdfImage);
//        document.close();
//
//        return pdfBaos.toByteArray();
//    }
//
//    // Helper: chuyển BufferedImage thành byte[]
//    private static byte[] toByteArray(BufferedImage image) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ImageIO.write(image, "png", baos);
//        return baos.toByteArray();
//    }
//}
