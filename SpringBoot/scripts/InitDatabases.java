import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class InitDatabases {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "12345678";

        String[] dbNames = {
            "db_auth", "db_otp", "db_entity", "db_payment", "db_media",
            "db_notification", "db_tour", "db_music", "db_film", "db_ai", "db_worker"
        };

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, pass);
                 Statement stmt = conn.createStatement()) {
                for (String db : dbNames) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + db + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    System.out.println("✅ Database verified/created: " + db);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
