using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using System.Data;

namespace BookHubAPI.Controllers
{
    [Route("api/auth")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        private readonly string connectionString = "Server=ADMIN-PC,1433;Database=bookhub_db;User Id=sa;Password=123456;TrustServerCertificate=True;";

        // 1. API ĐĂNG KÝ
        [HttpPost("register")]
        public IActionResult Register([FromBody] RegisterRequest request)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();

                    // Kiểm tra user tồn tại
                    string checkSql = "SELECT COUNT(*) FROM Users WHERE username = @username OR email = @email";
                    using (SqlCommand checkCmd = new SqlCommand(checkSql, conn))
                    {
                        checkCmd.Parameters.AddWithValue("@username", request.Username);
                        checkCmd.Parameters.AddWithValue("@email", request.Email);
                        int count = (int)checkCmd.ExecuteScalar();

                        if (count > 0)
                        {
                            return BadRequest(new { status = "Error", message = "Tài khoản hoặc Email đã tồn tại!" });
                        }
                    }

                    // Thêm user mới
                    string insertSql = "INSERT INTO Users (full_name, email, username, password_hash) VALUES (@fullName, @email, @username, @password)";
                    using (SqlCommand insertCmd = new SqlCommand(insertSql, conn))
                    {
                        insertCmd.Parameters.AddWithValue("@fullName", request.FullName);
                        insertCmd.Parameters.AddWithValue("@email", request.Email);
                        insertCmd.Parameters.AddWithValue("@username", request.Username);
                        insertCmd.Parameters.AddWithValue("@password", request.Password);
                        insertCmd.ExecuteNonQuery();
                    }
                }

                return Ok(new { status = "Success", message = "Đăng ký thành công!" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { status = "Error", message = "Lỗi Server: " + ex.Message });
            }
        }

        // 2. API ĐĂNG NHẬP
        [HttpPost("login")]
        public IActionResult Login([FromBody] LoginRequest request)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();

                    string sql = "SELECT user_id, full_name, email FROM Users WHERE username = @username AND password_hash = @password";
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@username", request.Username);
                        cmd.Parameters.AddWithValue("@password", request.Password);

                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            if (reader.Read())
                            {
                                // Lấy thông tin user
                                var user = new
                                {
                                    Id = reader["user_id"],
                                    FullName = reader["full_name"],
                                    Email = reader["email"],
                                    Username = request.Username
                                };

                                return Ok(new
                                {
                                    token = "TOKEN_DEMO_CSHARP_123",
                                    user = user
                                });
                            }
                            else
                            {
                                return Unauthorized(new { status = "Error", message = "Sai tài khoản hoặc mật khẩu" });
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { status = "Error", message = "Lỗi Server: " + ex.Message });
            }
        }
    }

    // --- CÁC CLASS DỮ LIỆU (DTO) ---
    public class RegisterRequest
    {
        public string FullName { get; set; }
        public string Email { get; set; }
        public string Username { get; set; }
        public string Password { get; set; }
    }

    public class LoginRequest
    {
        public string Username { get; set; }
        public string Password { get; set; }
    }
}