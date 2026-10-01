package com.liochio.ai.controller;

import com.liochio.common.constant.MessageConstants;
import com.liochio.common.context.TenantContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.common.i18n.MessageService;
import com.liochio.ai.entity.AiChatMessageEntity;
import com.liochio.ai.entity.AiChatSessionEntity;
import com.liochio.ai.repository.AiChatMessageRepository;
import com.liochio.ai.repository.AiChatSessionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/ai", "/api/ai"})
@RequiredArgsConstructor
@Tag(name = "AI Chatbot Controller", description = "Dịch vụ độc lập quản lý trợ lý AI RAG, hội thoại và kho tri thức")
public class AiChatbotController {

    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;
    private final MessageService messageService;

    @PostMapping("/chat/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Khởi tạo phiên hội thoại mới với Chatbot AI")
    public ApiResponse<AiChatSessionEntity> createSession() {
        String tenantId = TenantContext.getTenantId();
        AiChatSessionEntity session = AiChatSessionEntity.builder()
                .tenantId(tenantId)
                .sessionToken(UUID.randomUUID().toString())
                .build();
        return ApiResponse.created(sessionRepository.save(session), messageService.getMessage(MessageConstants.MSG_AI_SESSION_CREATED));
    }

    @GetMapping("/chat/sessions/{sessionId}/messages")
    @Operation(summary = "Lấy lịch sử tin nhắn của một phiên hội thoại")
    public ApiResponse<List<AiChatMessageEntity>> getMessages(@PathVariable Long sessionId) {
        return ApiResponse.success(messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId));
    }

    @PostMapping("/chat/sessions/{sessionId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gửi tin nhắn và nhận phản hồi từ AI Assistant")
    public ApiResponse<AiChatMessageEntity> sendMessage(
            @PathVariable Long sessionId,
            @RequestBody AiChatMessageEntity request
    ) {
        AiChatSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Lưu tin nhắn của User
        request.setSessionId(sessionId);
        request.setSenderType("USER");
        messageRepository.save(request);

        // Sinh phản hồi từ AI Cố Vấn Tài Chính Thông Minh
        String replyText = generateFinancialAdvisorReply(request.getMessageText(), session.getTenantId());
        AiChatMessageEntity assistantReply = AiChatMessageEntity.builder()
                .sessionId(sessionId)
                .senderType("ASSISTANT")
                .messageText(replyText)
                .tokensUsed(Math.max(25, replyText.length() / 4))
                .build();
        AiChatMessageEntity savedReply = messageRepository.save(assistantReply);

        session.setLastActivityAt(Instant.now());
        sessionRepository.save(session);

        return ApiResponse.created(savedReply, messageService.getMessage(MessageConstants.MSG_AI_MESSAGE_SENT));
    }

    private String generateFinancialAdvisorReply(String userQuery, String tenantId) {
        if (userQuery == null || userQuery.isBlank()) {
            return "Xin chào! Tôi là Trợ lý Cố vấn Tài chính Thông minh Liochio FinTech. Tôi có thể hỗ trợ gì cho bạn hôm nay?";
        }
        String lower = userQuery.toLowerCase();
        if (lower.contains("tiết kiệm") || lower.contains("heo") || lower.contains("piggy")) {
            return "Với mục tiêu tiết kiệm, Liochio cung cấp Heo Đất Thông Minh IoT tích hợp quỹ khẩn cấp và hũ chi tiêu. Bạn có thể thiết lập quy tắc trích lập tự động 10-20% thu nhập mỗi khi nhận lương để tạo thói quen tích lũy vững chắc.";
        } else if (lower.contains("hạn mức") || lower.contains("chuyển tiền") || lower.contains("limit")) {
            return "Hạn mức giao dịch hàng ngày của bạn được bảo vệ qua hệ thống Customer Onboarding Gates (Tier 1: 5.000.000 VNĐ, Tier 2: 50.000.000 VNĐ, Tier 3: 500.000.000 VNĐ kèm tài khoản ký quỹ chuyên dụng). Bạn có thể nâng cấp định danh sinh trắc học để tăng hạn mức.";
        } else if (lower.contains("ngân sách") || lower.contains("chi tiêu") || lower.contains("50/30/20")) {
            return "Quy tắc 50/30/20 khuyến nghị: 50% thu nhập cho nhu cầu thiết yếu (tiền nhà, ăn uống, hóa đơn), 30% cho sở thích cá nhân, và 20% cho quỹ tiết kiệm & đầu tư tương lai qua hệ thống Liochio Vault.";
        } else if (lower.contains("đầu tư") || lower.contains("lãi suất")) {
            return "Liochio FinTech tích hợp các sản phẩm sinh lời tự động từ số dư nhàn rỗi với lãi suất lũy tiến theo ngày. Vui lòng tham khảo phân hệ Vault & Asset Management để biết thêm chi tiết.";
        }
        return "Tôi là Trợ lý Tài chính Liochio AI. Tôi đã ghi nhận yêu cầu: \"" + userQuery + "\". Tôi luôn sẵn sàng tư vấn về quản lý dòng tiền, số dư tài khoản Sổ Cái (Ledger), lập kế hoạch ngân sách và phân bổ tài sản an toàn.";
    }
}
