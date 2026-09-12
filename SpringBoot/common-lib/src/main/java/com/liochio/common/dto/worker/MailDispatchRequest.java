package com.liochio.common.dto.worker;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailDispatchRequest {

    private String traceId;

    @NotBlank(message = "Người nhận không được để trống")
    private String recipient;

    @Builder.Default
    private String channel = "EMAIL";

    @NotBlank(message = "Mã mẫu thông báo không được để trống")
    private String templateCode;

    @Builder.Default
    private String languageCode = "vi";

    private String subject;

    private String customContent;

    private Map<String, Object> templateData;

    @Builder.Default
    private boolean sendImmediately = false;
}
