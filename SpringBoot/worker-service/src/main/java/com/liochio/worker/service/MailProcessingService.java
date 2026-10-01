package com.liochio.worker.service;

import com.liochio.worker.dto.MailDispatchRequest;
import com.liochio.worker.dto.MailDispatchResponse;
import com.liochio.worker.entity.DlqMessageEntity;
import com.liochio.worker.entity.MailLogEntity;
import com.liochio.worker.entity.MessageTemplateEntity;
import com.liochio.worker.repository.DlqMessageRepository;
import com.liochio.worker.repository.MailLogRepository;
import com.liochio.worker.repository.MessageTemplateRepository;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailProcessingService {

    private final MailLogRepository mailLogRepository;
    private final MessageTemplateRepository templateRepository;
    private final DlqMessageRepository dlqRepository;
    private final Optional<JavaMailSender> defaultMailSender;
    private final JdbcTemplate jdbcTemplate;

    public static final int MAX_RETRY_ATTEMPTS = 3;

    @Transactional
    public MailDispatchResponse dispatch(MailDispatchRequest request) {
        String traceId = (request.getTraceId() != null && !request.getTraceId().isBlank())
                ? request.getTraceId()
                : UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        String lang = (request.getLanguageCode() != null && !request.getLanguageCode().isBlank())
                ? request.getLanguageCode() : "vi";

        // 1. Kiểm tra cấu hình Override email từ DB (system_configs)
        String targetRecipient = resolveRecipient(request.getRecipient());

        // 2. Render template
        String subject = request.getCustomSubject();
        String content = request.getCustomContent();

        if (subject == null || content == null) {
            Optional<MessageTemplateEntity> tplOpt = templateRepository.findByTemplateCodeAndLanguageCodeAndIsActiveTrue(
                    request.getTemplateCode(), lang
            );
            if (tplOpt.isEmpty()) {
                tplOpt = templateRepository.findByTemplateCodeAndIsActiveTrue(request.getTemplateCode());
            }

            if (tplOpt.isPresent()) {
                MessageTemplateEntity tpl = tplOpt.get();
                if (subject == null) subject = renderVariables(tpl.getSubject(), request.getTemplateVariables());
                if (content == null) content = renderVariables(tpl.getBodyHtml(), request.getTemplateVariables());
            } else {
                if (subject == null) subject = "Thông báo từ Liochio Enterprise";
                if (content == null) content = "Nội dung thông báo (Mã mẫu: " + request.getTemplateCode() + ")";
            }
        }

        if (!targetRecipient.equalsIgnoreCase(request.getRecipient())) {
            subject = "[Gửi tới " + request.getRecipient() + "] " + subject;
            content = "<div style=\"background: #FEF3C7; padding: 10px 15px; border-left: 4px solid #F59E0B; margin-bottom: 20px; font-family: Arial, sans-serif; font-size: 13px; color: #92400E;\">"
                    + "<b>THÔNG BÁO TEST HỆ THỐNG</b><br/>"
                    + "Người nhận gốc: <b>" + request.getRecipient() + "</b><br/>"
                    + "Chuyển tiếp tới: <b>" + targetRecipient + "</b>"
                    + "</div>"
                    + content;
        }

        MailLogEntity entity = MailLogEntity.builder()
                .traceId(traceId)
                .recipient(targetRecipient)
                .channel(request.getChannel() != null ? request.getChannel() : "EMAIL")
                .templateCode(request.getTemplateCode())
                .languageCode(lang)
                .subject(subject)
                .content(content)
                .status("PENDING")
                .retryCount(0)
                .executionTimeMs(0L)
                .createdAt(Instant.now())
                .build();

        MailLogEntity saved = mailLogRepository.save(entity);

        // Nếu gửi đồng bộ trực tiếp
        if (request.getAsync() != null && !request.getAsync()) {
            processSingleMail(saved);
        }

        return mapToResponse(saved);
    }

    @Transactional
    public int processPendingBatch() {
        List<MailLogEntity> pendingList = mailLogRepository.findTop50ByStatusInOrderByCreatedAtAsc(
                Arrays.asList("PENDING", "PROCESSING")
        );

        if (pendingList.isEmpty()) {
            return 0;
        }

        log.info("[MailProcessingService] Đang xử lý {} email trong hàng đợi...", pendingList.size());
        int processedCount = 0;

        for (MailLogEntity mail : pendingList) {
            processSingleMail(mail);
            processedCount++;
        }

        return processedCount;
    }

    public void processSingleMail(MailLogEntity mail) {
        long startTime = System.currentTimeMillis();
        try {
            mail.setStatus("PROCESSING");
            mailLogRepository.save(mail);

            String finalRecipient = resolveRecipient(mail.getRecipient());
            if (!finalRecipient.equalsIgnoreCase(mail.getRecipient())) {
                mail.setRecipient(finalRecipient);
            }

            // Gửi email qua SMTP với chuẩn UTF-8 MIME
            if ("EMAIL".equalsIgnoreCase(mail.getChannel())) {
                sendSmtpEmail(mail.getRecipient(), mail.getSubject(), mail.getContent());
            }

            long latency = System.currentTimeMillis() - startTime;
            mail.setStatus("SENT");
            mail.setExecutionTimeMs(latency);
            mail.setErrorMessage(null);
            mailLogRepository.save(mail);

            log.info("\n" +
                    "================================================================================\n" +
                    "📧 [MAIL ENGINE] PHÁT THƯ TÍN UTF-8 THÀNH CÔNG (SENT)\n" +
                    "   -> Người nhận thực tế : {}\n" +
                    "   -> Tiêu đề thư        : {}\n" +
                    "   -> Mã Mẫu             : {}\n" +
                    "   -> Trace ID           : {}\n" +
                    "   -> Độ trễ xử lý       : {} ms\n" +
                    "================================================================================",
                    mail.getRecipient(), mail.getSubject(), mail.getTemplateCode(), mail.getTraceId(), latency);

        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            int currentRetry = mail.getRetryCount() + 1;
            mail.setRetryCount(currentRetry);
            mail.setExecutionTimeMs(latency);
            mail.setErrorMessage(e.getMessage());

            if (currentRetry >= MAX_RETRY_ATTEMPTS) {
                mail.setStatus("FAILED");
                DlqMessageEntity dlq = DlqMessageEntity.builder()
                        .eventId("mail_" + mail.getId())
                        .topic("EMAIL_DELIVERY_FAILURE")
                        .traceId(mail.getTraceId())
                        .payload(String.format("{\"recipient\":\"%s\",\"subject\":\"%s\"}", mail.getRecipient(), mail.getSubject()))
                        .retryCount(currentRetry)
                        .errorReason("Vượt quá số lần thử lại tối đa: " + e.getMessage())
                        .status("PENDING_RETRY")
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                dlqRepository.save(dlq);
                log.error("[MailProcessingService] ❌ Gửi email thất bại quá 3 lần -> Chuyển vào DLQ: ID={}", mail.getId());
            } else {
                mail.setStatus("PENDING");
            }

            mailLogRepository.save(mail);
        }
    }

    private void sendSmtpEmail(String to, String subject, String content) {
        JavaMailSender sender = buildDynamicMailSender();
        if (sender != null) {
            String effectiveRecipient = resolveRecipient(to);
            try {
                MimeMessage mimeMessage = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                String fromEmail = getConfigValue("smtp.from_email", getConfigValue("smtp.username", "no-reply@liochio.vn"));
                helper.setFrom(new InternetAddress(fromEmail, "Liochio FinTech Platform", "UTF-8"));
                helper.setTo(effectiveRecipient);

                String finalSubject = subject;
                if (!effectiveRecipient.equalsIgnoreCase(to)) {
                    finalSubject = "[Chuyển tiếp từ: " + to + "] " + subject;
                }
                helper.setSubject(finalSubject);

                String prefixBanner = "";
                if (!effectiveRecipient.equalsIgnoreCase(to)) {
                    prefixBanner = "<div style=\"background-color: #fef3c7; border: 1px solid #f59e0b; color: #92400e; padding: 12px; border-radius: 8px; margin-bottom: 16px; font-family: sans-serif;\">"
                            + "⚠️ <strong>Thông báo Hệ thống:</strong> Email này được chuyển tiếp tự động từ người nhận không tồn tại/kiểm thử: <code>" + to + "</code> tới hòm thư mặc định được cấu hình trong DB."
                            + "</div>";
                }

                if (content != null && (content.contains("<div") || content.contains("<h") || content.contains("<p"))) {
                    helper.setText(prefixBanner + content, true);
                } else {
                    String htmlBody = "<div style=\"font-family: Arial, sans-serif; font-size: 15px; color: #1F2937; line-height: 1.6; padding: 15px;\">"
                            + prefixBanner
                            + (content != null ? content.replace("\n", "<br/>") : subject)
                            + "</div>";
                    helper.setText(htmlBody, true);
                }

                sender.send(mimeMessage);
                log.info("[MailProcessingService] 📨 Đã chuyển thông điệp UTF-8 MIME qua SMTP tới '{}' (Địa chỉ gốc: '{}')", effectiveRecipient, to);
            } catch (Exception e) {
                log.warn("[MailProcessingService] Gửi qua SMTP Socket gặp lỗi: {} (Đang thử nghiệm phương án dự phòng...)", e.getMessage());
                // Fallback lần 2 nếu gửi cho email gốc thất bại do hòm thư không tồn tại
                String fallbackAllowed = getConfigValue("mail.fallback_to_default_recipient", "true");
                String fallbackMail = getConfigValue("mail.default_recipient", "voduylebt99@gmail.com");
                if (!effectiveRecipient.equalsIgnoreCase(fallbackMail) && ("1".equals(fallbackAllowed) || "true".equalsIgnoreCase(fallbackAllowed))) {
                    try {
                        log.info("[MailProcessingService] 🔄 Đang gửi lại thư tới email mặc định dự phòng '{}'...", fallbackMail);
                        MimeMessage fallbackMsg = sender.createMimeMessage();
                        MimeMessageHelper fallbackHelper = new MimeMessageHelper(fallbackMsg, true, "UTF-8");
                        String fromEmail = getConfigValue("smtp.from_email", getConfigValue("smtp.username", "no-reply@liochio.vn"));
                        fallbackHelper.setFrom(new InternetAddress(fromEmail, "Liochio FinTech Platform", "UTF-8"));
                        fallbackHelper.setTo(fallbackMail);
                        fallbackHelper.setSubject("[DỰ PHÒNG CHUYỂN TIẾP - Gốc: " + to + "] " + subject);
                        String notice = "<p style='color:red;'><b>Ghi chú hệ thống:</b> Gửi tới hòm thư gốc " + to + " thất bại (" + e.getMessage() + "), đã chuyển tiếp tự động tới hòm thư mặc định.</p>";
                        fallbackHelper.setText(notice + content, true);
                        sender.send(fallbackMsg);
                        log.info("[MailProcessingService] 📨 Đã gửi lại thành công qua SMTP tới hòm thư mặc định '{}'", fallbackMail);
                    } catch (Exception ex2) {
                        log.warn("[MailProcessingService] Gửi lại tới hòm thư mặc định cũng thất bại: {}", ex2.getMessage());
                    }
                }
            }
        }
    }

    private JavaMailSender buildDynamicMailSender() {
        String host = getConfigValue("smtp.host", "smtp.gmail.com");
        int port = Integer.parseInt(getConfigValue("smtp.port", "587"));
        String username = getConfigValue("smtp.username", "");
        String password = getConfigValue("smtp.password", "");

        if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
            JavaMailSenderImpl impl = new JavaMailSenderImpl();
            impl.setHost(host);
            impl.setPort(port);
            impl.setUsername(username);
            impl.setPassword(password);
            impl.setDefaultEncoding("UTF-8");

            Properties props = impl.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.debug", "false");
            return impl;
        }

        return defaultMailSender.orElse(null);
    }

    public String resolveRecipient(String defaultRecipient) {
        try {
            // 1. Kiểm tra cấu hình override toàn bộ
            String isOverride = getConfigValue("mail.override_enabled", "0");
            if ("1".equals(isOverride) || "true".equalsIgnoreCase(isOverride)) {
                String overrideEmail = getConfigValue("mail.override_recipient", "");
                if (overrideEmail != null && !overrideEmail.isBlank()) {
                    return overrideEmail.trim();
                }
            }

            // 2. Kiểm tra tính năng chuyển tiếp email mặc định khi email người nhận không có thật / test
            String fallbackAllowed = getConfigValue("mail.fallback_to_default_recipient", "true");
            if ("1".equals(fallbackAllowed) || "true".equalsIgnoreCase(fallbackAllowed)) {
                if (isDummyOrNonExistentEmail(defaultRecipient)) {
                    String defaultFallbackMail = getConfigValue("mail.default_recipient", "voduylebt99@gmail.com");
                    log.info("[MailProcessingService] ⚠️ Người nhận '{}' là email kiểm thử/không tồn tại. " +
                            "Theo cấu hình DB (mail.fallback_to_default_recipient=true), chuyển tiếp tới email mặc định: '{}'",
                            defaultRecipient, defaultFallbackMail);
                    return defaultFallbackMail;
                }
            }
        } catch (Exception e) {
            log.debug("[MailProcessingService] Lỗi đọc mail.override: {}", e.getMessage());
        }
        return defaultRecipient;
    }

    private boolean isDummyOrNonExistentEmail(String email) {
        if (email == null || email.isBlank()) {
            return true;
        }
        String clean = email.trim().toLowerCase();
        if (!clean.contains("@") || !clean.contains(".") || clean.length() < 6) {
            return true;
        }
        if (clean.equals("abc@gmail.com") || clean.startsWith("test") || clean.startsWith("dummy")
                || clean.startsWith("sample") || clean.contains("example.com") || clean.endsWith(".test")
                || clean.equals("user@gmail.com")) {
            return true;
        }
        return false;
    }

    private String getConfigValue(String key, String defaultValue) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT config_value FROM liochio_core_db.system_configs WHERE config_key = ? AND is_active = 1 LIMIT 1",
                    String.class,
                    key
            );
        } catch (Exception ignored) {
            try {
                return jdbcTemplate.queryForObject(
                        "SELECT `value` FROM liochio_app_db.system_settings WHERE `key` = ? AND status = 'ACTIVE' LIMIT 1",
                        String.class,
                        key
                );
            } catch (Exception ignored2) {
                return defaultValue;
            }
        }
    }

    private String renderVariables(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String val = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            result = result.replace(placeholder, val);
        }
        return result;
    }

    public MailDispatchResponse mapToResponse(MailLogEntity entity) {
        return MailDispatchResponse.builder()
                .id(entity.getId())
                .traceId(entity.getTraceId())
                .recipient(entity.getRecipient())
                .channel(entity.getChannel())
                .templateCode(entity.getTemplateCode())
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .executionTimeMs(entity.getExecutionTimeMs())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
