package com.liochio.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ==============================================================================
 * Annotation Chống Trùng Lặp Request Phân Tán (Idempotency Annotation)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đặt trên các endpoint quan trọng (thanh toán, tạo đơn hàng, trừ tiền, gửi OTP)
 *   để ngăn chặn việc Client hoặc Network bấm 2 lần gây nhân đôi giao dịch.
 * - Sử dụng Redis SetNX (Distributed Lock) dựa trên Header `Idempotency-Key`.
 * 
 * Khi nào gọi:
 * - AOP Filter chặn ngay khi request chạm tới Controller.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * Thời gian khóa giữ key trên Redis (tính theo giây, mặc định 120s)
     */
    long timeoutSeconds() default 120L;

    /**
     * Có bắt buộc phải truyền Header Idempotency-Key hay không
     */
    boolean required() default true;
}
