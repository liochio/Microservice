package com.liochio.common.pattern.strategy;

import com.liochio.common.enums.StorageProvider;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * ==============================================================================
 * Chiến Lược Lưu Trữ Tệp Tin (Storage Strategy Interface - GoF Strategy)
 * ==============================================================================
 * 
 * Mục đích:
 * - Định nghĩa giao diện chuẩn cho các cơ chế lưu trữ Media (Local, Cloudinary, AWS S3 / R2).
 * - Cho phép hoán đổi nhà cung cấp lưu trữ mà không phải sửa code logic nghiệp vụ.
 */
public interface StorageStrategy {

    /**
     * Trả về loại Storage Provider mà Strategy này đảm nhiệm
     */
    StorageProvider getProvider();

    /**
     * Tải lên toàn bộ tệp tin
     *
     * @param file       Tệp tin từ Multipart request
     * @param folderPath Thư mục lưu trữ trên Cloud / Local
     * @return URL công khai của tệp tin sau khi tải lên
     */
    String uploadFile(MultipartFile file, String folderPath);

    /**
     * Tải lên một phần của tệp tin trong cơ chế Chunk Upload
     *
     * @param inputStream Luồng byte của chunk
     * @param fileName    Tên tệp tin
     * @param chunkIndex  Thứ tự chunk
     * @param totalChunks Tổng số chunk
     * @return Đường dẫn tạm thời hoặc URL hoàn tất khi gộp xong
     */
    String uploadChunk(InputStream inputStream, String fileName, int chunkIndex, int totalChunks);

    /**
     * Xóa tệp tin khỏi bộ lưu trữ
     *
     * @param publicId / fileUrl Mã định danh tệp tin
     * @return true nếu xóa thành công
     */
    boolean deleteFile(String publicId);
}
