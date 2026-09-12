package com.liochio.entityservice.controller;

import com.liochio.common.annotation.RequirePermission;
import com.liochio.common.constant.MessageConstants;
import com.liochio.common.dto.ApiResponse;
import com.liochio.common.i18n.MessageService;
import com.liochio.entityservice.dto.FormDefinitionRequest;
import com.liochio.entityservice.dto.FormDefinitionResponse;
import com.liochio.entityservice.service.FormDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ==============================================================================
 * Controller Định Nghĩa Biểu Mẫu Nhập Liệu Động (Form Definition Controller)
 * ==============================================================================
 */
@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
@Tag(name = "Form Definition Controller", description = "Các API định nghĩa form nhập liệu và validate động")
public class FormDefinitionController {

    private final FormDefinitionService formDefinitionService;
    private final MessageService messageService;

    @GetMapping("/{formCode}")
    @Operation(summary = "Lấy cấu trúc định nghĩa form theo formCode")
    public ApiResponse<FormDefinitionResponse> getByFormCode(@PathVariable String formCode) {
        FormDefinitionResponse response = formDefinitionService.getByFormCode(formCode);
        return ApiResponse.success(response);
    }

    @PostMapping
    @RequirePermission("system:admin")
    @Operation(summary = "Lưu hoặc cập nhật định nghĩa form")
    public ApiResponse<FormDefinitionResponse> saveOrUpdate(@Valid @RequestBody FormDefinitionRequest request) {
        FormDefinitionResponse response = formDefinitionService.saveOrUpdate(request);
        return ApiResponse.success(response, messageService.getMessage(MessageConstants.MSG_FORM_SAVED));
    }
}
