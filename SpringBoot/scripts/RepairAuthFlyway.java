import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class RepairAuthFlyway {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/db_auth?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345678";

    public static void main(String[] args) {
        System.out.println("=== KIEM TRA VA SUA CHUA FLYWAY SCHEMA HISTORY TRONG DB_AUTH ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT installed_rank, version, description, type, script, checksum, success FROM flyway_schema_history_auth ORDER BY installed_rank");
            System.out.println("Cac ban ghi hien tai trong flyway_schema_history_auth:");
            while (rs.next()) {
                System.out.printf("  Rank %d | Version: %s | Description: %s | Checksum: %s | Success: %b\n",
                        rs.getInt("installed_rank"),
                        rs.getString("version"),
                        rs.getString("description"),
                        rs.getString("checksum"),
                        rs.getBoolean("success"));
            }

            int deleted = stmt.executeUpdate("DELETE FROM flyway_schema_history_auth WHERE success = 0 OR success IS FALSE");
            System.out.printf("  -> Da xoa %d ban ghi migration bi loi (success = 0).\n", deleted);

            System.out.println("=== HOAN TAT SUA CHUA ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}