import com.google.gson.Gson;
import java.sql.*;
import static spark.Spark.*;

public class ApiServer {

    // 🔴 THAY ĐỔI THÔNG SỐ KẾT NỐI CỦA BẠN TẠI ĐÂY
    private static final String DB_URL = "jdbc:sqlserver://ADMIN-PC:1433;databaseName=bookhub_db;encrypt=false;";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "123456";
    
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        
        // 1. Cấu hình cổng chạy API (Mặc định là 4567, chúng ta đổi sang 8080)
        port(8080);
        
        // 2. Định nghĩa API endpoint ĐĂNG KÝ
        post("/api/auth/register", (request, response) -> {
            response.type("application/json");
            
            // Chuyển JSON request body thành đối tượng Java
            RegisterRequest regRequest = gson.fromJson(request.body(), RegisterRequest.class);
            
            // --- LOGIC XỬ LÝ ĐĂNG KÝ ---
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                
                // 1. Kiểm tra tồn tại
                if (isUserExists(conn, regRequest.getUsername(), regRequest.getEmail())) {
                    response.status(400); // Bad Request
                    return gson.toJson(new ApiResponse("Error", "Tên đăng nhập hoặc Email đã tồn tại."));
                }
                
                // 2. Lưu vào DB (Không băm để đơn giản, nhưng KHÔNG NÊN làm trong thực tế)
                String sql = "INSERT INTO Users (full_name, username, email, password_hash) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, regRequest.getFullName());
                    pstmt.setString(2, regRequest.getUsername());
                    pstmt.setString(3, regRequest.getEmail());
                    // ⚠️ CHÚ Ý: Đang lưu mật khẩu thô (plaintext) cho mục đích đơn giản hóa. 
                    // Trong thực tế phải BĂM mật khẩu!
                    pstmt.setString(4, regRequest.getPassword()); 
                    
                    pstmt.executeUpdate();
                }
                
                response.status(200);
                return gson.toJson(new ApiResponse("Success", "Đăng ký thành công!"));
                
            } catch (SQLException e) {
                e.printStackTrace();
                response.status(500); // Internal Server Error
                return gson.toJson(new ApiResponse("Error", "Lỗi Server Database: " + e.getMessage()));
            }
        });

        // 3. Định nghĩa API endpoint ĐĂNG NHẬP
        post("/api/auth/login", (request, response) -> {
            response.type("application/json");
            
            // Chuyển JSON request body thành đối tượng Java
            LoginRequest loginRequest = gson.fromJson(request.body(), LoginRequest.class);
            
            // --- LOGIC XỬ LÝ ĐĂNG NHẬP ---
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                
                String sql = "SELECT username FROM Users WHERE username = ? AND password_hash = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, loginRequest.getUsername());
                    // ⚠️ So sánh trực tiếp với mật khẩu thô (plaintext)
                    pstmt.setString(2, loginRequest.getPassword());
                    
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (rs.next()) {
                        // Đăng nhập thành công
                        response.status(200);
                        // Trả về một token giả
                        String token = "DUMMY_TOKEN_" + loginRequest.getUsername();
                        return gson.toJson(new LoginResponse(token, loginRequest.getUsername()));
                    } else {
                        // Sai tên đăng nhập hoặc mật khẩu
                        response.status(401); // Unauthorized
                        return gson.toJson(new ApiResponse("Error", "Tên đăng nhập hoặc mật khẩu không đúng."));
                    }
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                response.status(500);
                return gson.toJson(new ApiResponse("Error", "Lỗi Server Database: " + e.getMessage()));
            }
        });
        
        System.out.println("API Server is running on port 8080. Start connecting from Android App!");
    }
    
    // Hàm hỗ trợ kiểm tra người dùng đã tồn tại
    private static boolean isUserExists(Connection conn, String username, String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users WHERE username = ? OR email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // --- CÁC CLASS DTO ĐƠN GIẢN ---
    private static class RegisterRequest {
        private String fullName;
        private String username;
        private String email;
        private String password;
        // Bỏ qua confirmPassword
    }
    
    private static class LoginRequest {
        private String username;
        private String password;
    }
    
    private static class LoginResponse {
        private String token;
        private String username;
        
        public LoginResponse(String token, String username) {
            this.token = token;
            this.username = username;
        }
    }
    
    private static class ApiResponse {
        private String status;
        private String message;
        
        public ApiResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}