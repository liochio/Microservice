package com.liochio.common.entity;

import com.liochio.common.constant.AppConstants;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

/**
 * ==============================================================================
 * Lớp Thực Thể Cơ Sở Toàn Hệ Thống (Base Entity - Auditing, Soft Delete & Multi-Tenancy)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cung cấp các trường dùng chung cho 100% Entity trong toàn bộ các Microservices:
 *   1. Khóa chính `id` (Auto Increment)
 *   2. Định danh khách thuê `tenant_id` và Hibernate Filter cô lập dữ liệu tự động
 *   3. JPA Auditing tự động lưu `created_at`, `updated_at`, `created_by`, `last_modified_by`
 *   4. Cơ chế xóa mềm `is_deleted` (Soft Delete)
 * - Sử dụng Instant (UTC) cho toàn bộ thời gian lưu trữ cơ sở dữ liệu.
 * 
 * Khi nào gọi:
 * - 100% các Entity nghiệp vụ (User, Role, Portfolio, DynamicEntity, Media, Payment...)
 *   phải kế thừa (extends) BaseEntity này.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Khóa chính duy nhất của bản ghi
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Mã định danh khách thuê (Multi-Tenancy Isolation)
     */
    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId = AppConstants.DEFAULT_TENANT_ID;

    /**
     * Thời điểm tạo bản ghi (UTC ISO-8601)
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /**
     * Thời điểm chỉnh sửa gần nhất (UTC ISO-8601)
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    /**
     * Tài khoản người thực hiện tạo mới
     */
    @CreatedBy
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * Tài khoản người thực hiện cập nhật sau cùng
     */
    @LastModifiedBy
    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;

    /**
     * Khóa lạc quan (Optimistic Locking) chống xung đột cập nhật đồng thời
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Cờ đánh dấu xóa mềm (true: đã xóa, false: đang hoạt động)
     */
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    /**
     * Thời điểm thực hiện xóa mềm (UTC ISO-8601)
     */
    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Người thực hiện xóa mềm
     */
    @Column(name = "deleted_by")
    private Long deletedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public boolean isDeleted() { return isDeleted; }
    public boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public Long getDeletedBy() { return deletedBy; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }
}
