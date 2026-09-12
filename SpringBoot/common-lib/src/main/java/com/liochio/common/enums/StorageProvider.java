package com.liochio.common.enums;

import lombok.Getter;

/**
 * ==============================================================================
 * Enum Nhà Cung Cấp Lưu Trữ Tệp Tin (Storage Providers)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định danh các giải pháp lưu trữ Media trong Media Service (Strategy Pattern):
 *   + LOCAL: Lưu trữ trực tiếp trên ổ đĩa máy chủ (Môi trường Dev / Test)
 *   + CLOUDINARY: Lưu trữ và tối ưu hóa hình ảnh đám mây Cloudinary
 *   + S3_COMPATIBLE: Lưu trữ S3 / Cloudflare R2 / AWS S3 / MinIO
 * 
 * Khi nào gọi:
 * - Được StorageStrategyFactory khởi tạo và chuyển đổi linh hoạt qua cấu hình database.
 */
@Getter
public enum StorageProvider {
    LOCAL("Lưu trữ Local Server"),
    CLOUDINARY("Lưu trữ Cloudinary CDN"),
    S3_COMPATIBLE("Lưu trữ AWS S3 / Cloudflare R2 / MinIO");

    private final String description;

    StorageProvider(String description) {
        this.description = description;
    }
}
