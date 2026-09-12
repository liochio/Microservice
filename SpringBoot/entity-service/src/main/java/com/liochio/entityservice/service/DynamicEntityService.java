package com.liochio.entityservice.service;

import com.liochio.common.context.TenantContext;
import com.liochio.common.context.UserContext;
import com.liochio.common.dto.PageResponse;
import com.liochio.common.exception.AppException;
import com.liochio.common.exception.ErrorCode;
import com.liochio.entityservice.dto.DynamicEntityRequest;
import com.liochio.entityservice.dto.DynamicEntityResponse;
import com.liochio.entityservice.dto.DynamicEntityRevisionResponse;
import com.liochio.entityservice.dto.EntityStatusUpdateRequest;
import com.liochio.entityservice.entity.DynamicEntity;
import com.liochio.entityservice.entity.DynamicEntityRevisionEntity;
import com.liochio.entityservice.repository.DynamicEntityRepository;
import com.liochio.entityservice.repository.DynamicEntityRevisionRepository;
import com.liochio.entityservice.validator.DynamicSchemaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ==============================================================================
 * Dịch Vụ Quản Lý Thực Thể Động Nâng Cao (Enterprise Dynamic Entity Engine)
 * ==============================================================================
 * 
 * Tính năng chính:
 * 1. Dynamic Schema Validation: Đối chiếu thuộc tính động với dynamic_field_definitions.
 * 2. Multi-Level Caching: Tích hợp Spring Cache (L1 Caffeine + L2 Redis).
 * 3. Revision History & Rollback: Tự động lưu snapshot phiên bản và hỗ trợ hoàn tác dữ liệu.
 * 4. Content Workflow State Machine: Kiểm soát chuyển trạng thái DRAFT -> PENDING_REVIEW -> PUBLISHED -> ARCHIVED.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicEntityService {

    private final DynamicEntityRepository dynamicEntityRepository;
    private final DynamicEntityRevisionRepository revisionRepository;
    private final DynamicSchemaValidator schemaValidator;

    private static final Set<String> VALID_STATUSES = Set.of("DRAFT", "PENDING_REVIEW", "PUBLISHED", "ARCHIVED", "REJECTED");

    @Transactional(readOnly = true)
    public PageResponse<DynamicEntityResponse> search(String entityType, String status, String keyword, Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        Specification<DynamicEntity> spec = com.liochio.entityservice.specification.DynamicEntitySpecification.filter(entityType, status, keyword, tenantId);
        Page<DynamicEntityResponse> page = dynamicEntityRepository.findAll(spec, pageable).map(this::mapToResponse);
        return PageResponse.from(page);
    }

    @Transactional
    @Cacheable(value = "dynamic_entities", key = "T(com.liochio.common.context.TenantContext).getTenantId() + ':' + #entityType + ':' + #slug", unless = "#result == null")
    public DynamicEntityResponse getBySlug(String entityType, String slug) {
        String tenantId = TenantContext.getTenantId();
        DynamicEntity entity = dynamicEntityRepository.findByTenantIdAndEntityTypeAndSlug(tenantId, entityType, slug)
                .or(() -> dynamicEntityRepository.findByEntityTypeAndSlug(entityType, slug))
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Tự động tăng lượt xem
        entity.setViewCount(entity.getViewCount() + 1);
        dynamicEntityRepository.save(entity);

        return mapToResponse(entity);
    }

    @Transactional
    @CacheEvict(value = "dynamic_entities", allEntries = true)
    public DynamicEntityResponse create(DynamicEntityRequest request) {
        String tenantId = TenantContext.getTenantId();
        String username = UserContext.getUsername();

        // 1. Dynamic Schema Validation
        schemaValidator.validate(request.getEntityType(), request.getAttributes());

        String initialStatus = request.getStatus() != null && VALID_STATUSES.contains(request.getStatus().toUpperCase())
                ? request.getStatus().toUpperCase()
                : "DRAFT";

        DynamicEntity entity = DynamicEntity.builder()
                .entityType(request.getEntityType().trim())
                .slug(request.getSlug().trim())
                .title(request.getTitle())
                .attributes(request.getAttributes())
                .status(initialStatus)
                .viewCount(0L)
                .build();
        entity.setTenantId(tenantId);

        DynamicEntity saved = dynamicEntityRepository.save(entity);

        // 2. Tạo Revision 1 (Snapshot ban đầu)
        createRevisionSnapshot(saved, 1, username, "Khởi tạo thực thể mới");

        log.info("[DynamicEntityService] Tạo mới Dynamic Entity ID: {}, Type: '{}', Revision: 1", saved.getId(), saved.getEntityType());
        return mapToResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "dynamic_entities", allEntries = true)
    public DynamicEntityResponse update(Long id, DynamicEntityRequest request) {
        DynamicEntity entity = dynamicEntityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        String username = UserContext.getUsername();

        // 1. Dynamic Schema Validation
        schemaValidator.validate(entity.getEntityType(), request.getAttributes());

        entity.setTitle(request.getTitle());
        entity.setSlug(request.getSlug().trim());
        entity.setAttributes(request.getAttributes());
        if (request.getStatus() != null && VALID_STATUSES.contains(request.getStatus().toUpperCase())) {
            entity.setStatus(request.getStatus().toUpperCase());
        }

        DynamicEntity updated = dynamicEntityRepository.save(entity);

        // 2. Tạo Revision tiếp theo
        int nextRev = getNextRevisionNumber(updated.getTenantId(), updated.getId());
        createRevisionSnapshot(updated, nextRev, username, "Cập nhật thông tin thực thể");

        log.info("[DynamicEntityService] Cập nhật Dynamic Entity ID: {}, Type: '{}', Revision: {}", updated.getId(), updated.getEntityType(), nextRev);
        return mapToResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "dynamic_entities", allEntries = true)
    public DynamicEntityResponse updateStatus(Long id, EntityStatusUpdateRequest request) {
        DynamicEntity entity = dynamicEntityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        String newStatus = request.getStatus().toUpperCase().trim();
        if (!VALID_STATUSES.contains(newStatus)) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Trạng thái không hợp lệ: " + newStatus);
        }

        validateStateTransition(entity.getStatus(), newStatus);

        entity.setStatus(newStatus);
        DynamicEntity updated = dynamicEntityRepository.save(entity);

        String username = UserContext.getUsername();
        int nextRev = getNextRevisionNumber(updated.getTenantId(), updated.getId());
        String reason = request.getReason() != null ? request.getReason() : ("Chuyển trạng thái sang " + newStatus);
        createRevisionSnapshot(updated, nextRev, username, reason);

        log.info("[DynamicEntityService] Chuyển trạng thái Dynamic Entity ID: {} sang '{}'", id, newStatus);
        return mapToResponse(updated);
    }

    @Transactional
    @CacheEvict(value = "dynamic_entities", allEntries = true)
    public DynamicEntityResponse rollback(Long id, Integer revisionNumber) {
        String tenantId = TenantContext.getTenantId();
        DynamicEntity entity = dynamicEntityRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        DynamicEntityRevisionEntity targetRevision = revisionRepository
                .findByTenantIdAndEntityIdAndRevisionNumber(tenantId, id, revisionNumber)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy phiên bản revision " + revisionNumber));

        // Khôi phục snapshot từ revision
        entity.setTitle(targetRevision.getTitle());
        entity.setSlug(targetRevision.getSlug());
        entity.setAttributes(targetRevision.getAttributesSnapshot());
        entity.setStatus(targetRevision.getStatus());

        DynamicEntity restored = dynamicEntityRepository.save(entity);

        String username = UserContext.getUsername();
        int nextRev = getNextRevisionNumber(restored.getTenantId(), restored.getId());
        createRevisionSnapshot(restored, nextRev, username, "Hoàn tác (Rollback) về phiên bản " + revisionNumber);

        log.info("[DynamicEntityService] Hoàn tác thành công Dynamic Entity ID: {} về revision {}", id, revisionNumber);
        return mapToResponse(restored);
    }

    @Transactional(readOnly = true)
    public List<DynamicEntityRevisionResponse> getRevisions(Long id) {
        String tenantId = TenantContext.getTenantId();
        if (!dynamicEntityRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        return revisionRepository.findByTenantIdAndEntityIdOrderByRevisionNumberDesc(tenantId, id).stream()
                .map(this::mapToRevisionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "dynamic_entities", allEntries = true)
    public void delete(Long id) {
        if (!dynamicEntityRepository.existsById(id)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        dynamicEntityRepository.deleteById(id);
    }

    private void validateStateTransition(String currentStatus, String targetStatus) {
        if (currentStatus.equals(targetStatus)) return;

        boolean allowed = switch (currentStatus) {
            case "DRAFT" -> targetStatus.equals("PENDING_REVIEW") || targetStatus.equals("PUBLISHED");
            case "PENDING_REVIEW" -> targetStatus.equals("PUBLISHED") || targetStatus.equals("REJECTED") || targetStatus.equals("DRAFT");
            case "REJECTED" -> targetStatus.equals("DRAFT") || targetStatus.equals("PENDING_REVIEW");
            case "PUBLISHED" -> targetStatus.equals("ARCHIVED") || targetStatus.equals("DRAFT");
            case "ARCHIVED" -> targetStatus.equals("DRAFT") || targetStatus.equals("PUBLISHED");
            default -> true;
        };

        if (!allowed) {
            throw new AppException(ErrorCode.INVALID_REQUEST,
                    String.format("Không thể chuyển trạng thái từ '%s' sang '%s'", currentStatus, targetStatus));
        }
    }

    private int getNextRevisionNumber(String tenantId, Long entityId) {
        return revisionRepository.findFirstByTenantIdAndEntityIdOrderByRevisionNumberDesc(tenantId, entityId)
                .map(r -> r.getRevisionNumber() + 1)
                .orElse(1);
    }

    private void createRevisionSnapshot(DynamicEntity entity, int revNum, String modifiedBy, String changeSummary) {
        DynamicEntityRevisionEntity rev = DynamicEntityRevisionEntity.builder()
                .tenantId(entity.getTenantId() != null ? entity.getTenantId() : "default")
                .entityId(entity.getId())
                .revisionNumber(revNum)
                .entityType(entity.getEntityType())
                .slug(entity.getSlug())
                .title(entity.getTitle())
                .attributesSnapshot(entity.getAttributes())
                .status(entity.getStatus())
                .modifiedBy(modifiedBy != null ? modifiedBy : "SYSTEM")
                .changeSummary(changeSummary)
                .createdAt(Instant.now())
                .build();

        revisionRepository.save(rev);
    }

    private DynamicEntityResponse mapToResponse(DynamicEntity entity) {
        return DynamicEntityResponse.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .entityType(entity.getEntityType())
                .slug(entity.getSlug())
                .title(entity.getTitle())
                .attributes(entity.getAttributes())
                .status(entity.getStatus())
                .viewCount(entity.getViewCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    private DynamicEntityRevisionResponse mapToRevisionResponse(DynamicEntityRevisionEntity r) {
        return DynamicEntityRevisionResponse.builder()
                .id(r.getId())
                .entityId(r.getEntityId())
                .revisionNumber(r.getRevisionNumber())
                .entityType(r.getEntityType())
                .slug(r.getSlug())
                .title(r.getTitle())
                .attributesSnapshot(r.getAttributesSnapshot())
                .status(r.getStatus())
                .modifiedBy(r.getModifiedBy())
                .changeSummary(r.getChangeSummary())
                .createdAt(r.getCreatedAt())
                .build();
    }
}