package com.liochio.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * ==============================================================================
 * Tiện Ích Chuyển Đổi Dữ Liệu JSON (JSON Utility)
 * ==============================================================================
 * 
 * Mục đích:
 * - Cung cấp ObjectMapper đã cấu hình tối ưu sẵn (hỗ trợ JavaTimeModule, Instant UTC,
 *   bỏ qua trường lạ `FAIL_ON_UNKNOWN_PROPERTIES = false`).
 * - Hỗ trợ parse JSON sang Java Object và ngược lại một cách an toàn.
 * 
 * Khi nào gọi:
 * - Được dùng trong Outbox Publisher, Event Listeners, Redis Serializers và Dynamic Engine.
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonUtils() {
        // Chống khởi tạo instance cho Utility Class
    }

    public static ObjectMapper getMapper() {
        return OBJECT_MAPPER;
    }

    public static String toJson(Object object) {
        if (object == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("[JsonUtils] Lỗi serialize Object sang JSON string: ", e);
            throw new RuntimeException("Lỗi serialize JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return null;
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("[JsonUtils] Lỗi deserialize JSON string sang Object: ", e);
            throw new RuntimeException("Lỗi deserialize JSON", e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) return null;
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            log.error("[JsonUtils] Lỗi deserialize JSON string sang Generic Type: ", e);
            throw new RuntimeException("Lỗi deserialize JSON", e);
        }
    }
}
