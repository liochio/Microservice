package com.liochio.common.annotation;

import com.liochio.common.enums.ActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ==============================================================================
 * Annotation Ghi Vết Kiểm Toán Toàn Diện (Enterprise Audit Logging)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đặt trên method Controller / Service để tự động ghi log vào bảng 'audit_logs'
 *   theo dõi mọi biến động dữ liệu, phân loại module, action, thời gian thực thi,
 *   IP, User, Tenant, Thiết bị và dữ liệu trước/sau (old_data, new_data).
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * Tên phân hệ nghiệp vụ (vd: "AUTH", "TOUR", "BOOKING", "PAYMENT", "MEDIA", "USER", "AI")
     */
    String module() default "CORE";

    /**
     * Loại hành động
     */
    ActionType action() default ActionType.VIEW;

    /**
     * Diễn giải tóm tắt hành động
     */
    String description() default "";

    /**
     * Có lưu dữ liệu Request Payload vào audit log hay không (mặc định TRUE)
     */
    boolean logRequestBody() default true;

    /**
     * Có lưu dữ liệu Response Payload vào audit log hay không (mặc định TRUE)
     */
    boolean logResponseBody() default true;
}
