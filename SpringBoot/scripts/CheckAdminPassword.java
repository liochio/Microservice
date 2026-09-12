import java.sql.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckAdminPassword {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/portfolio-engine?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "12345678";
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, pass);
            PreparedStatement stmt = conn.prepareStatement("SELECT id, tenant_id, username, email, password, status FROM users");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                long id = rs.getLong("id");
                String username = rs.getString("username");
                String passwordHash = rs.getString("password");
                String status = rs.getString("status");
                
                System.out.println("User ID: " + id + ", username: " + username + ", status: " + status);
                System.out.println("   Hash: " + passwordHash);
                
                String[] testPasswords = {
                    "password123", "12345678", "admin", "admin123", "Password@123456", "superadmin", 
                    "123456", "admin@123", "Admin@123", "Admin@123456", "password", "Password123", 
                    "Password123!", "admin123456", "12345678aA@"
                };
                for (String p : testPasswords) {
                    if (passwordHash != null && encoder.matches(p, passwordHash)) {
                        System.out.println("   --> MATCH FOUND for " + username + "! Password is: '" + p + "'");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
