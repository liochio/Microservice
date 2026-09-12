package com.liochio.notification.controller;

import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.i18n.MessageService;
import com.liochio.notification.dto.NotificationResponse;
import com.liochio.notification.dto.NotificationSendRequest;
import com.liochio.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ==============================================================================
 * Controller Phát Tán & Quản Trị Hộp Thư Thông Báo (Notification Hub Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "Các API gửi thông báo đa kênh, quản lý hộp thư và trạng thái đã đọc")
public class NotificationController {

    private final NotificationService notificationService;
    private final MessageService messageService;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gửi thông báo tới người nhận qua kênh chỉ định (Hỗ trợ Template & Idempotency Key)")
    public ApiResponse<NotificationResponse> send(@Valid @RequestBody NotificationSendRequest request) {
        NotificationResponse response = notificationService.send(request);
        return ApiResponse.created(response, messageService.getMessage(MessageConstants.MSG_NOTIFICATION_SENT));
    }

    @GetMapping("/history")
    @Operation(summary = "Lấy lịch sử thông báo đã gửi của tenant")
    public ApiResponse<List<NotificationResponse>> getHistory() {
        List<NotificationResponse> list = notificationService.getHistory();
        return ApiResponse.success(list);
    }

    @GetMapping("/my-inbox")
    @Operation(summary = "Lấy danh sách thông báo hộp thư cá nhân")
    public ApiResponse<List<NotificationResponse>> getMyInbox(
            @RequestParam(required = false) String recipient) {
        String effectiveRecipient = (recipient != null && !recipient.isBlank()) ? recipient : UserContext.getUsername();
        if (effectiveRecipient == null || effectiveRecipient.isBlank()) {
            effectiveRecipient = "guest";
        }
        List<NotificationResponse> list = notificationService.getMyInbox(effectiveRecipient);
        return ApiResponse.success(list);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Đánh dấu thông báo là đã đọc")
    public ApiResponse<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @RequestParam(required = false) String recipient) {
        String effectiveRecipient = (recipient != null && !recipient.isBlank()) ? recipient : UserContext.getUsername();
        NotificationResponse response = notificationService.markAsRead(id, effectiveRecipient);
        return ApiResponse.success(response, "Đã đánh dấu thông báo là đã đọc");
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo trong hộp thư là đã đọc")
    public ApiResponse<Integer> markAllAsRead(@RequestParam(required = false) String recipient) {
        String effectiveRecipient = (recipient != null && !recipient.isBlank()) ? recipient : UserContext.getUsername();
        int count = notificationService.markAllAsRead(effectiveRecipient);
        return ApiResponse.success(count, "Đã đánh dấu tất cả thông báo là đã đọc (" + count + " thông báo)");
    }
}
