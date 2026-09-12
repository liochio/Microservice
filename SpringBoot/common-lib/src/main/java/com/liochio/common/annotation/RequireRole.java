package com.liochio.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ==============================================================================
 * Annotation Phân Quyền Theo Vai Trò (Role-Based Access Control)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đặt trên method hoặc class của Controller / Service để giới hạn quyền truy cập
 *   dành riêng cho các Roles được chỉ định (vd: "ROLE_SUPER_ADMIN", "ROLE_CORP_ADMIN").
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * Danh sách vai trò hợp lệ (chỉ cần thỏa mãn ít nhất 1 role trong danh sách)
     */
    String[] value();
}
