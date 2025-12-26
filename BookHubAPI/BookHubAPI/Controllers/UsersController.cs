using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using Microsoft.Extensions.Configuration;
using System;

namespace BookHubAPI.Controllers
{
    [Route("api/users")]
    [ApiController]
    public class UsersController : ControllerBase
    {
        private readonly IConfiguration _configuration;
        public UsersController(IConfiguration configuration) { _configuration = configuration; }

        private string GetConnectionString() => _configuration.GetConnectionString("DefaultConnection");

        // 1. LẤY THÔNG TIN CHI TIẾT (GET /api/users/{id})
        [HttpGet("{id}")]
        public IActionResult GetUserProfile(int id)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    string sql = "SELECT user_id, full_name, email, phone_number, username, avatar_url FROM Users WHERE user_id = @id";
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            if (reader.Read())
                            {
                                return Ok(new
                                {
                                    id = reader["user_id"],
                                    fullName = reader["full_name"],
                                    email = reader["email"],
                                    phone = reader["phone_number"] != DBNull.Value ? reader["phone_number"] : "",
                                    username = reader["username"],
                                    avatar = reader["avatar_url"] != DBNull.Value ? reader["avatar_url"] : ""
                                });
                            }
                        }
                    }
                }
                return NotFound(new { message = "Không tìm thấy người dùng" });
            }
            catch (Exception ex) { return StatusCode(500, ex.Message); }
        }

        // 2. CẬP NHẬT THÔNG TIN (PUT /api/users/{id})
        [HttpPut("{id}")]
        public IActionResult UpdateProfile(int id, [FromBody] UpdateProfileRequest request)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    string sql = "UPDATE Users SET full_name = @name, email = @email, phone_number = @phone WHERE user_id = @id";
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        cmd.Parameters.AddWithValue("@name", request.FullName);
                        cmd.Parameters.AddWithValue("@email", request.Email); // Nếu không sửa email thì gửi email cũ lên
                        cmd.Parameters.AddWithValue("@phone", request.Phone ?? (object)DBNull.Value);

                        int rows = cmd.ExecuteNonQuery();
                        if (rows > 0) return Ok(new { message = "Cập nhật thành công!" });
                    }
                }
                return BadRequest(new { message = "Cập nhật thất bại" });
            }
            catch (Exception ex) { return StatusCode(500, ex.Message); }
        }

        // 3. ĐỔI MẬT KHẨU (POST /api/users/change-password)
        [HttpPost("change-password")]
        public IActionResult ChangePassword([FromBody] ChangePasswordRequest request)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    // Bước 1: Kiểm tra mật khẩu cũ
                    string checkSql = "SELECT COUNT(*) FROM Users WHERE user_id = @id AND password_hash = @oldPass";
                    using (SqlCommand checkCmd = new SqlCommand(checkSql, conn))
                    {
                        checkCmd.Parameters.AddWithValue("@id", request.UserId);
                        checkCmd.Parameters.AddWithValue("@oldPass", request.OldPassword);
                        int count = (int)checkCmd.ExecuteScalar();

                        if (count == 0) return BadRequest(new { message = "Mật khẩu cũ không đúng!" });
                    }

                    // Bước 2: Cập nhật mật khẩu mới
                    string updateSql = "UPDATE Users SET password_hash = @newPass WHERE user_id = @id";
                    using (SqlCommand updateCmd = new SqlCommand(updateSql, conn))
                    {
                        updateCmd.Parameters.AddWithValue("@id", request.UserId);
                        updateCmd.Parameters.AddWithValue("@newPass", request.NewPassword);
                        updateCmd.ExecuteNonQuery();
                    }
                }
                return Ok(new { message = "Đổi mật khẩu thành công!" });
            }
            catch (Exception ex) { return StatusCode(500, ex.Message); }
        }

        // 4. LẤY DANH SÁCH SÁCH ĐÃ ĐÁNH GIÁ (GET /api/users/{id}/reviews)
        [HttpGet("{id}/reviews")]
        public IActionResult GetUserReviews(int id)
        {
            var list = new List<object>();
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    string sql = @"SELECT r.rating, r.comment, r.created_at, b.title, b.image_file 
                                   FROM Reviews r 
                                   JOIN Books b ON r.book_id = b.book_id 
                                   WHERE r.user_id = @uid ORDER BY r.created_at DESC";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", id);
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                string img = reader["image_file"] != DBNull.Value ? reader["image_file"].ToString() : "";
                                if (!string.IsNullOrEmpty(img) && !img.StartsWith("http"))
                                    img = $"{Request.Scheme}://{Request.Host}/images/{img}";

                                list.Add(new
                                {
                                    bookTitle = reader["title"],
                                    rating = Convert.ToInt32(reader["rating"]),
                                    comment = reader["comment"].ToString(),
                                    date = Convert.ToDateTime(reader["created_at"]).ToString("dd/MM/yyyy"),
                                    image = img
                                });
                            }
                        }
                    }
                }
                return Ok(list);
            }
            catch (Exception ex) { return StatusCode(500, ex.Message); }
        }
    }

    // --- DTO CLASSES ---
    public class UpdateProfileRequest
    {
        public string FullName { get; set; }
        public string Email { get; set; }
        public string Phone { get; set; }
    }

    public class ChangePasswordRequest
    {
        public int UserId { get; set; }
        public string OldPassword { get; set; }
        public string NewPassword { get; set; }
    }
}