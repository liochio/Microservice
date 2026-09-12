package com.liochio.entityservice.service;

import com.liochio.common.context.TenantContext;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.entityservice.dto.FormDefinitionRequest;
import com.liochio.entityservice.dto.FormDefinitionResponse;
import com.liochio.entityservice.entity.FormDefinitionEntity;
import com.liochio.entityservice.repository.FormDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ==============================================================================
 * Dịch Vụ Quản Lý Biểu Mẫu Nhập Liệu Động (Form Definition Service)
 * ==============================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormDefinitionService {

    private final FormDefinitionRepository formDefinitionRepository;

    @Transactional(readOnly = true)
    public FormDefinitionResponse getByFormCode(String formCode) {
        String tenantId = TenantContext.getTenantId();
        FormDefinitionEntity entity = formDefinitionRepository.findByTenantIdAndFormCode(tenantId, formCode)
                .or(() -> formDefinitionRepository.findByFormCode(formCode))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return FormDefinitionResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .formCode(entity.getFormCode())
                .title(entity.getTitle())
                .fieldDefinitions(entity.getFieldDefinitions())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Transactional
    public FormDefinitionResponse saveOrUpdate(FormDefinitionRequest request) {
        String tenantId = TenantContext.getTenantId();

        FormDefinitionEntity entity = formDefinitionRepository.findByTenantIdAndFormCode(tenantId, request.getFormCode())
                .orElseGet(() -> {
                    FormDefinitionEntity newEntity = new FormDefinitionEntity();
                    newEntity.setTenantId(tenantId);
                    newEntity.setFormCode(request.getFormCode());
                    return newEntity;
                });

        entity.setTitle(request.getTitle());
        entity.setFieldDefinitions(request.getFieldDefinitions());

        FormDefinitionEntity saved = formDefinitionRepository.save(entity);
        log.info("[FormDefinitionService] Đã lưu form definition '{}'", saved.getFormCode());

        return FormDefinitionResponse.builder()
                .id(saved.getId())
                .tenantId(saved.getTenantId())
                .formCode(saved.getFormCode())
                .title(saved.getTitle())
                .fieldDefinitions(saved.getFieldDefinitions())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }
}
