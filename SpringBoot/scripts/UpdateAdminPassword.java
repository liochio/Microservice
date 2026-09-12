import java.sql.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UpdateAdminPassword {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/portfolio-engine?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "12345678";
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String newHash = encoder.encode("password123");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET password = ?, failed_login_attempts = 0, lockout_until = NULL, status = 'ACTIVE' WHERE username = 'admin' OR username = 'superadmin'"
                );
                stmt.setString(1, newHash);
                int count = stmt.executeUpdate();
                System.out.println("Updated " + count + " users password to 'password123' (hash: " + newHash + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
