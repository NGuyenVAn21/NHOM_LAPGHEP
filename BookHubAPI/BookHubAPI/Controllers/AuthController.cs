using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using System.Data;
using Microsoft.Extensions.Configuration; // Thư viện để đọc appsettings.json

namespace BookHubAPI.Controllers
{
    [Route("api/auth")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        // 1. Khai báo biến cấu hình
        private readonly IConfiguration _configuration;

        // 2. Inject cấu hình vào Constructor
        public AuthController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        private string GetConnectionString()
        {
            return _configuration.GetConnectionString("DefaultConnection");
        }

        [HttpPost("register")]
        public IActionResult Register([FromBody] RegisterRequest request)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
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

        [HttpPost("login")]
        public IActionResult Login([FromBody] LoginRequest request)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
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

    // --- DTO CLASSES ---
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