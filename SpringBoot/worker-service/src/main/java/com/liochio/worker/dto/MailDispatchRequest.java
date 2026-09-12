package com.liochio.worker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailDispatchRequest {

    private String traceId;

    @NotBlank(message = "Recipient email/phone is required")
    private String recipient;

    @Builder.Default
    private String channel = "EMAIL";

    @NotBlank(message = "Template code is required")
    private String templateCode;

    @Builder.Default
    private String languageCode = "vi";

    private String customSubject;
    private String customContent;
    private Map<String, Object> templateVariables;
    private Boolean async;
}
