package com.liochio.common.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.liochio.common.deserializer.DynamicSanitizeDeserializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ==============================================================================
 * Annotation Tự Động Làm Sạch & Chống XSS (Dynamic Data Sanitization)
 * ==============================================================================
 * 
 * Mục đích:
 * - Đặt trên các trường String của DTO để tự động kích hoạt bộ Deserializer loại bỏ
 *   mã độc XSS (script tags, javascript: links) hoặc lọc ký tự đặc biệt ngay khi
 *   Spring tiếp nhận payload JSON từ Client.
 * 
 * Khi nào gọi:
 * - Jackson Deserialization gọi khi parse JSON Request Body thành đối tượng Java DTO.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = DynamicSanitizeDeserializer.class)
public @interface DynamicSanitize {

    /**
     * Khóa quy tắc lọc (vd: "HTML", "STRIP_SPECIAL", "SQL_SAFE")
     * Mặc định là "HTML" để loại bỏ các thẻ HTML / XSS nguy hiểm.
     */
    String ruleKey() default "HTML";
}