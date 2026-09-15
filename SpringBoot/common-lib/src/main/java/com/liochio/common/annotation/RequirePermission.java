package com.liochio.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ==============================================================================
 * Annotation Kiểm Tra Phân Quyền Động RBAC / ABAC (Dynamic Permission Check)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đặt trên method của Controller hoặc Service để kiểm tra quyền hạn tài nguyên
 *   theo định dạng 'resource:action' (vd: 'tour:create', 'booking:refund', 'user:delete').
 * - Hỗ trợ kiểm tra đơn lẻ hoặc danh sách quyền với chế độ 'ALL' hoặc 'ANY'.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * Danh sách mã quyền hạn bắt buộc (vd: {"tour:create", "tour:update"})
     */
    String[] value();

    /**
     * Chế độ kiểm tra: ALL (phải có đủ mọi quyền) hoặc ANY (chỉ cần có ít nhất 1 quyền)
     */
    Mode mode() default Mode.ALL;

    /**
     * Bắt buộc phải là Quản trị viên tối cao (SUPER_ADMIN)
     */
    boolean superAdminOnly() default false;

    enum Mode {
        ALL,
        ANY
    }
}
