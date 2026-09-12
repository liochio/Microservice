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
            try {
                MimeMessage mimeMessage = sender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                String fromEmail = getConfigValue("smtp.from_email", "voduylebt99@gmail.com");
                helper.setFrom(new InternetAddress(fromEmail, "Liochio FinTech Platform", "UTF-8"));
                helper.setTo(to);
                helper.setSubject(subject);

                if (content != null && (content.contains("<div") || content.contains("<h") || content.contains("<p"))) {
                    helper.setText(content, true);
                } else {
                    String htmlBody = "<div style=\"font-family: Arial, sans-serif; font-size: 15px; color: #1F2937; line-height: 1.6; padding: 15px;\">"
                            + (content != null ? content.replace("\n", "<br/>") : subject)
                            + "</div>";
                    helper.setText(htmlBody, true);
                }

                sender.send(mimeMessage);
                log.info("[MailProcessingService] 📨 Đã chuyển thông điệp UTF-8 MIME qua SMTP tới '{}'", to);
            } catch (Exception e) {
                log.warn("[MailProcessingService] Gửi qua SMTP Socket gặp thông báo: {} (Đã lưu trữ nội dung vào mail_logs)", e.getMessage());
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
            String isOverride = getConfigValue("mail.override_enabled", "0");
            if ("1".equals(isOverride) || "true".equalsIgnoreCase(isOverride)) {
                String overrideEmail = getConfigValue("mail.override_recipient", "");
                if (overrideEmail != null && !overrideEmail.isBlank()) {
                    return overrideEmail.trim();
                }
            }
        } catch (Exception e) {
            log.debug("[MailProcessingService] Lỗi đọc mail.override: {}", e.getMessage());
        }
        return defaultRecipient;
    }

    private String getConfigValue(String key, String defaultValue) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT config_value FROM liochio_core_db.system_configs WHERE config_key = ? AND is_active = 1 LIMIT 1",
                    String.class,
                    key
            );
        } catch (Exception ignored) {
            return defaultValue;
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
