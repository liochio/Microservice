import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

public class MigrateAllDatabases {
    public static void main(String[] args) {
        String baseUrl = "jdbc:mysql://localhost:3306/";
        String params = "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&allowMultiQueries=true&characterEncoding=UTF-8";
        String user = "root";
        String pass = "12345678";

        Map<String, String> migrations = new LinkedHashMap<>();
        migrations.put("db_auth", "auth-service/src/main/resources/db/migration");
        migrations.put("db_otp", "otp-service/src/main/resources/db/migration");
        migrations.put("db_entity", "entity-service/src/main/resources/db/migration");
        migrations.put("db_media", "media-service/src/main/resources/db/migration");
        migrations.put("db_notification", "notification-service/src/main/resources/db/migration");
        migrations.put("db_payment", "payment-service/src/main/resources/db/migration");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            for (Map.Entry<String, String> entry : migrations.entrySet()) {
                String db = entry.getKey();
                String folder = entry.getValue();
                
                System.out.println("\n🚀 Migrating Database: `" + db + "` from " + folder);
                
                // Đảm bảo database tồn tại
                try (Connection conn = DriverManager.getConnection(baseUrl + params, user, pass);
                     Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + db + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                }
                
                File dir = new File(folder);
                if (!dir.exists()) {
                    System.out.println("   Folder not found: " + folder);
                    continue;
                }
                
                File[] sqlFiles = dir.listFiles((d, name) -> name.endsWith(".sql"));
                if (sqlFiles == null || sqlFiles.length == 0) {
                    System.out.println("   No SQL files in " + folder);
                    continue;
                }
                
                Arrays.sort(sqlFiles, Comparator.comparing(File::getName));
                
                try (Connection conn = DriverManager.getConnection(baseUrl + db + params, user, pass)) {
                    for (File f : sqlFiles) {
                        System.out.println("   Executing: " + f.getName());
                        String content = Files.readString(f.toPath());
                        
                        // Tách các câu lệnh và thực thi
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(content);
                        } catch (SQLException e) {
                            System.out.println("      ⚠️ Note on " + f.getName() + ": " + e.getMessage());
                        }
                    }
                }
                System.out.println("   ✅ Migrations for `" + db + "` applied successfully!");
            }
            
            // Khởi tạo bảng cho db_ai
            try (Connection conn = DriverManager.getConnection(baseUrl + "db_ai" + params, user, pass);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `tenant_ai_configs` ("
                        + "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, "
                        + "`tenant_id` VARCHAR(50) NOT NULL UNIQUE, "
                        + "`provider` VARCHAR(50) NOT NULL DEFAULT 'OPENAI', "
                        + "`api_key` VARCHAR(255), "
                        + "`model_name` VARCHAR(100) NOT NULL DEFAULT 'gpt-4o', "
                        + "`system_prompt` TEXT, "
                        + "`temperature` DOUBLE DEFAULT 0.7, "
                        + "`is_enabled` BOOLEAN NOT NULL DEFAULT TRUE, "
                        + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `ai_chat_sessions` ("
                        + "`id` VARCHAR(64) PRIMARY KEY, "
                        + "`tenant_id` VARCHAR(50) NOT NULL, "
                        + "`user_id` BIGINT, "
                        + "`title` VARCHAR(255), "
                        + "`status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', "
                        + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                        + "`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `ai_chat_messages` ("
                        + "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, "
                        + "`session_id` VARCHAR(64) NOT NULL, "
                        + "`role` VARCHAR(20) NOT NULL, "
                        + "`content` LONGTEXT NOT NULL, "
                        + "`tokens_consumed` INT DEFAULT 0, "
                        + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `ai_knowledge_base` ("
                        + "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, "
                        + "`tenant_id` VARCHAR(50) NOT NULL, "
                        + "`document_name` VARCHAR(255) NOT NULL, "
                        + "`content_chunk` LONGTEXT NOT NULL, "
                        + "`metadata` JSON, "
                        + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `ai_prompts` ("
                        + "`id` BIGINT AUTO_INCREMENT PRIMARY KEY, "
                        + "`tenant_id` VARCHAR(50) NOT NULL, "
                        + "`prompt_code` VARCHAR(100) NOT NULL UNIQUE, "
                        + "`template_content` TEXT NOT NULL, "
                        + "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                
                System.out.println("\n   ✅ Migrations for `db_ai` applied successfully!");
            }
            
            // Khởi tạo bảng cho db_worker (Shedlock)
            try (Connection conn = DriverManager.getConnection(baseUrl + "db_worker" + params, user, pass);
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `shedlock` ("
                        + "`name` VARCHAR(64) NOT NULL PRIMARY KEY, "
                        + "`lock_until` TIMESTAMP(3) NOT NULL, "
                        + "`locked_at` TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), "
                        + "`locked_by` VARCHAR(255) NOT NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                
                System.out.println("   ✅ Migrations for `db_worker` applied successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
