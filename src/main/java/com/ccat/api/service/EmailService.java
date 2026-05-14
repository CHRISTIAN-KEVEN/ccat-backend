package com.ccat.api.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender       mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.mail.from-address}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Async
    public void sendVerificationEmail(String to, String otp) {
        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("to", to);
        ctx.setVariable("otp", otp);
        sendHtml(to, "CCAT — Verify your email address", "email/verification", ctx);
    }

    @Async
    public void sendOtp(String to, String otp) {
        Context ctx = new Context(Locale.ENGLISH);
        ctx.setVariable("to", to);
        ctx.setVariable("otp", otp);
        sendHtml(to, "CCAT — Your password reset code", "email/reset-password", ctx);
    }

    // -------------------------------------------------------------------------

    private void sendHtml(String to, String subject, String template, Context ctx) {
        try {
            log.info("Sending '{}' to {}", subject, to);
            String html = templateEngine.process(template, ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Email '{}' sent successfully to {}", subject, to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email '{}' to {} — {}", subject, to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {} — {}", to, e.getMessage(), e);
        }
    }
}
