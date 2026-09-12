import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckTenantsTable {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/db_auth?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345678";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("DESCRIBE tenants");
            System.out.println("Cac cot trong bang tenants:");
            while (rs.next()) {
                System.out.printf("  %s (%s)\n", rs.getString(1), rs.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}