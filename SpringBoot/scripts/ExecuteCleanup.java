import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ExecuteCleanup {
    public static void main(String[] args) {
        String baseUrl = "jdbc:mysql://localhost:3306/";
        String params = "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&allowMultiQueries=true&characterEncoding=UTF-8";
        String user = "root";
        String pass = "12345678";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(baseUrl + params, user, pass);
                 Statement stmt = conn.createStatement()) {
                
                System.out.println("🧹 1. Xóa các Database trùng lặp và rác...");
                stmt.executeUpdate("DROP DATABASE IF EXISTS `portfolio_otp`");
                System.out.println("   ✅ Dropped `portfolio_otp`");
                stmt.executeUpdate("DROP DATABASE IF EXISTS `portfolio-engine`");
                System.out.println("   ✅ Dropped `portfolio-engine`");
                stmt.executeUpdate("DROP DATABASE IF EXISTS `db_ai`");
                System.out.println("   ✅ Dropped `db_ai`");
                stmt.executeUpdate("DROP DATABASE IF EXISTS `db_entity`");
                System.out.println("   ✅ Dropped `db_entity`");

                System.out.println("\n🧹 2. Dọn dẹp bảng rác trong `db_content_eav`...");
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `db_content_eav` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                stmt.executeUpdate("USE `db_content_eav`");
                stmt.executeUpdate("DROP TABLE IF EXISTS `ai_chat_messages`");
                stmt.executeUpdate("DROP TABLE IF EXISTS `ai_chat_sessions`");
                stmt.executeUpdate("DROP TABLE IF EXISTS `ai_knowledge_base`");
                stmt.executeUpdate("DROP TABLE IF EXISTS `tenant_ai_configs`");
                stmt.executeUpdate("DROP TABLE IF EXISTS `portfolio_items`");
                System.out.println("   ✅ Cleaned `ai_*`, `tenant_ai_configs`, `portfolio_items` from `db_content_eav`");

                System.out.println("\n🧹 3. Dọn dẹp bảng `user_tokens` trong `db_auth`...");
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `db_auth` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                stmt.executeUpdate("USE `db_auth`");
                stmt.executeUpdate("DROP TABLE IF EXISTS `user_tokens`");
                System.out.println("   ✅ Dropped `user_tokens` from `db_auth`");

                System.out.println("\n🚀 4. Khởi tạo `db_ai_vector` (nếu chưa có)...");
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `db_ai_vector` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                stmt.executeUpdate("USE `db_ai_vector`");
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
                System.out.println("   ✅ Verified tables in `db_ai_vector`");

                System.out.println("\n🎉 HOÀN TẤT DỌN DẸP VÀ CHUẨN HÓA CƠ SỞ DỮ LIỆU!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
