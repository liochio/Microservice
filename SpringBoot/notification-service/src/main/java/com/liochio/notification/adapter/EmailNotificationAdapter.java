package com.liochio.notification.adapter;

import com.liochio.common.enums.NotificationChannel;
import com.liochio.common.pattern.strategy.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ==============================================================================
 * Bộ Chuyển Đổi Gửi Email (Email Notification Adapter - SMTP / Mailpit)
 * ==============================================================================
 */
@Slf4j
@Component
public class EmailNotificationAdapter implements NotificationStrategy {

    private final JavaMailSender mailSender;

    public EmailNotificationAdapter(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean send(String recipient, String subject, String content, Map<String, Object> metadata) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@portfolio-engine.dev");
            message.setTo(recipient);
            message.setSubject(subject != null ? subject : "Thông báo từ Portfolio Engine");
            message.setText(content);

            mailSender.send(message);
            log.info("[EmailAdapter] Gửi email thành công tới: {}", recipient);
            return true;
        } catch (Exception e) {
            log.error("[EmailAdapter] Gửi email thất bại tới {}: {}", recipient, e.getMessage());
            return false;
        }
    }
}
