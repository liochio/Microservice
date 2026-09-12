package com.liochio.media.adapter;

import com.liochio.common.enums.StorageProvider;
import com.liochio.common.pattern.strategy.StorageStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * ==============================================================================
 * Bộ Chuyển Đổi Lưu Trữ Local Server (Local Storage Adapter)
 * ==============================================================================
 */
@Slf4j
@Component
public class LocalStorageAdapter implements StorageStrategy {

    private static final String UPLOAD_DIR = "uploads";

    @Override
    public StorageProvider getProvider() {
        return StorageProvider.LOCAL;
    }

    @Override
    public String uploadFile(MultipartFile file, String folderPath) {
        try {
            Path targetDir = Paths.get(UPLOAD_DIR, folderPath != null ? folderPath : "");
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf(".")) : "";
            String uniqueName = UUID.randomUUID().toString().replace("-", "") + extension;

            Path targetPath = targetDir.resolve(uniqueName);
            file.transferTo(targetPath.toFile());

            log.info("[LocalStorage] Đã lưu tệp tin: {}", targetPath);
            return "/api/media/files/" + uniqueName;
        } catch (Exception e) {
            log.error("[LocalStorage] Lỗi lưu tệp tin: ", e);
            throw new RuntimeException("Lỗi upload local file", e);
        }
    }

    @Override
    public String uploadChunk(InputStream inputStream, String fileName, int chunkIndex, int totalChunks) {
        try {
            Path tempDir = Paths.get(UPLOAD_DIR, "chunks", fileName);
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            Path chunkPath = tempDir.resolve("chunk_" + chunkIndex);
            try (FileOutputStream fos = new FileOutputStream(chunkPath.toFile())) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            // Nếu đã nhận đủ toàn bộ chunk -> Thực hiện gộp (merge)
            if (chunkIndex == totalChunks - 1) {
                Path mergedDir = Paths.get(UPLOAD_DIR, "merged");
                if (!Files.exists(mergedDir)) {
                    Files.createDirectories(mergedDir);
                }
                Path mergedPath = mergedDir.resolve(fileName);
                try (FileOutputStream mergedFos = new FileOutputStream(mergedPath.toFile())) {
                    for (int i = 0; i < totalChunks; i++) {
                        Path currentChunk = tempDir.resolve("chunk_" + i);
                        if (Files.exists(currentChunk)) {
                            Files.copy(currentChunk, mergedFos);
                            Files.deleteIfExists(currentChunk);
                        }
                    }
                }
                Files.deleteIfExists(tempDir);
                log.info("[LocalStorage] Gộp thành công toàn bộ {} chunks thành: {}", totalChunks, mergedPath);
                return "/api/media/files/merged/" + fileName;
            }

            return "Chunk " + chunkIndex + " uploaded successfully";
        } catch (Exception e) {
            log.error("[LocalStorage] Lỗi xử lý chunk upload: ", e);
            throw new RuntimeException("Lỗi xử lý chunk upload", e);
        }
    }

    @Override
    public boolean deleteFile(String publicId) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, publicId);
            return Files.deleteIfExists(filePath);
        } catch (Exception e) {
            log.error("[LocalStorage] Lỗi xóa tệp tin: ", e);
            return false;
        }
    }
}
