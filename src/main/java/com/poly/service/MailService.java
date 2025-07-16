package com.poly.service;

public interface MailService {
    void sendCertificateMail(String to, String subject, String content);

    void sendCertificateMailWithAttachment(String to, String subject, String content, byte[] pdfBytes, String fileName);
}