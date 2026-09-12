package com.liochio.media.adapter;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.liochio.common.enums.StorageProvider;
import com.liochio.common.pattern.strategy.StorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

/**
 * ==============================================================================
 * Bộ Chuyển Đổi Lưu Trữ Đám Mây Cloudinary (Cloudinary Storage Adapter)
 * ==============================================================================
 */
@Slf4j
@Component
public class CloudinaryStorageAdapter implements StorageStrategy {

    private final Cloudinary cloudinary;

    public CloudinaryStorageAdapter(
            @Value("${cloudinary.cloud-name:demo}") String cloudName,
            @Value("${cloudinary.api-key:123456789}") String apiKey,
            @Value("${cloudinary.api-secret:secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public StorageProvider getProvider() {
        return StorageProvider.CLOUDINARY;
    }

    @Override
    public String uploadFile(MultipartFile file, String folderPath) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folderPath != null ? folderPath : "portfolio")
            );
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("[Cloudinary] Upload ảnh thành công: {}", secureUrl);
            return secureUrl;
        } catch (Exception e) {
            log.error("[Cloudinary] Lỗi upload ảnh lên Cloudinary: ", e);
            throw new RuntimeException("Lỗi upload Cloudinary", e);
        }
    }

    @Override
    public String uploadChunk(InputStream inputStream, String fileName, int chunkIndex, int totalChunks) {
        // Fallback sang Local cho chunk upload
        return "Cloudinary chunk stream initiated";
    }

    @Override
    public boolean deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return true;
        } catch (Exception e) {
            log.error("[Cloudinary] Lỗi xóa tệp tin trên Cloudinary: ", e);
            return false;
        }
    }
}
