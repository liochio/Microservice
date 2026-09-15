package com.liochio.common.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.liochio.common.annotation.DynamicSanitize;
import com.liochio.common.utils.SanitizerUtils;

import java.io.IOException;

/**
 * ==============================================================================
 * Bộ Giải Mã JSON Tuỳ Biến Tự Động Làm Sạch (Dynamic Sanitize Deserializer)
 * ==============================================================================
 * 
 * Mục đích:
 * - Tích hợp trực tiếp vào quá trình Jackson Deserialization để tự động làm sạch
 *   nội dung chuỗi String trước khi dữ liệu chạm tới Controller.
 * 
 * Khi nào gọi:
 * - Được Jackson kích hoạt khi bắt gặp annotation '@DynamicSanitize' trên trường của DTO.
 */
public class DynamicSanitizeDeserializer extends StdDeserializer<String> implements ContextualDeserializer {

    private static final long serialVersionUID = 1L;
    private String ruleKey = "HTML";

    public DynamicSanitizeDeserializer() {
        super(String.class);
    }

    public DynamicSanitizeDeserializer(String ruleKey) {
        super(String.class);
        this.ruleKey = ruleKey;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        return SanitizerUtils.sanitize(value, this.ruleKey);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            DynamicSanitize annotation = property.getAnnotation(DynamicSanitize.class);
            if (annotation != null) {
                return new DynamicSanitizeDeserializer(annotation.ruleKey());
            }
        }
        return this;
    }
}