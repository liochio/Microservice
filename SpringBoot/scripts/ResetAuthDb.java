import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ResetAuthDb {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345678";

    public static void main(String[] args) {
        System.out.println("=== RESETTING db_auth SCHEMA FOR CLEAN FLYWAY MIGRATION ===");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("DROP DATABASE IF EXISTS db_auth");
            stmt.executeUpdate("CREATE DATABASE db_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("-> db_auth dropped and recreated cleanly.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}