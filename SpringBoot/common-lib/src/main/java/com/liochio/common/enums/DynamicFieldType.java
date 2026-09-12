package com.liochio.common.enums;

import lombok.Getter;

/**
 * ==============================================================================
 * Enum Kiểu Dữ liệu Trường Động (Dynamic Field Types)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa các kiểu dữ liệu cho Schema động của mô hình Hybrid EAV + JSONB
 *   trong bảng dynamic_entities và form_definitions (Server-Driven UI).
 * 
 * Khi nào gọi:
 * - Được Entity Service sử dụng khi tạo bảng thuộc tính động, validate input từ user
 *   và sinh layout form cho Frontend ReactJS render tương ứng.
 */
@Getter
public enum DynamicFieldType {
    TEXT("Chuỗi ký tự đơn giản"),
    RICH_TEXT("Đoạn văn bản định dạng HTML / Markdown"),
    NUMBER("Số nguyên hoặc số thực"),
    BOOLEAN("Giá trị Đúng / Sai (true/false)"),
    DATE_TIME("Thời gian ngày giờ ISO-8601"),
    IMAGE_URL("Đường dẫn hình ảnh CDN / S3"),
    FILE_URL("Đường dẫn tệp tin đính kèm"),
    JSON_OBJECT("Cấu trúc JSON lồng nhau phức tạp"),
    ARRAY("Mảng danh sách các phần tử");

    private final String description;

    DynamicFieldType(String description) {
        this.description = description;
    }
}
