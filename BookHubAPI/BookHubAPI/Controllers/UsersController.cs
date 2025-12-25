using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using System.Collections.Generic;
using System;
using Microsoft.Extensions.Configuration;

namespace BookHubAPI.Controllers
{
    [Route("api/users")]
    [ApiController]
    public class UsersController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public UsersController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        private string GetConnectionString()
        {
            return _configuration.GetConnectionString("DefaultConnection");
        }

        // ==================== GET ALL USERS ====================
        [HttpGet]
        public IActionResult GetAllUsers()
        {
            var users = new List<object>();
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    string sql = @"
                        SELECT user_id, username, email, full_name, phone_number, 
                               avatar_url, created_at, is_active
                        FROM Users
                        ORDER BY user_id DESC
                    ";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                string avatarUrl = reader["avatar_url"] != DBNull.Value ? reader["avatar_url"].ToString() : "";
                                if (!string.IsNullOrEmpty(avatarUrl) && !avatarUrl.StartsWith("http"))
                                {
                                    avatarUrl = $"{Request.Scheme}://{Request.Host}/images/{avatarUrl}";
                                }

                                users.Add(new
                                {
                                    userId = Convert.ToInt32(reader["user_id"]),
                                    username = reader["username"].ToString(),
                                    email = reader["email"].ToString(),
                                    fullName = reader["full_name"].ToString(),
                                    phoneNumber = reader["phone_number"] != DBNull.Value ? reader["phone_number"].ToString() : null,
                                    avatarUrl = avatarUrl,
                                    createdAt = Convert.ToDateTime(reader["created_at"]),
                                    isActive = Convert.ToBoolean(reader["is_active"])
                                });
                            }
                        }
                    }
                }
                return Ok(users);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi Server: " + ex.Message });
            }
        }

        // ==================== GET USER BY ID ====================
        [HttpGet("{id}")]
        public IActionResult GetUserById(int id)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    string sql = @"
                        SELECT user_id, username, email, full_name, phone_number, 
                               avatar_url, created_at, is_active
                        FROM Users
                        WHERE user_id = @id
                    ";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            if (reader.Read())
                            {
                                string avatarUrl = reader["avatar_url"] != DBNull.Value ? reader["avatar_url"].ToString() : "";
                                if (!string.IsNullOrEmpty(avatarUrl) && !avatarUrl.StartsWith("http"))
                                {
                                    avatarUrl = $"{Request.Scheme}://{Request.Host}/images/{avatarUrl}";
                                }

                                return Ok(new
                                {
                                    userId = Convert.ToInt32(reader["user_id"]),
                                    username = reader["username"].ToString(),
                                    email = reader["email"].ToString(),
                                    fullName = reader["full_name"].ToString(),
                                    phoneNumber = reader["phone_number"] != DBNull.Value ? reader["phone_number"].ToString() : null,
                                    avatarUrl = avatarUrl,
                                    createdAt = Convert.ToDateTime(reader["created_at"]),
                                    isActive = Convert.ToBoolean(reader["is_active"])
                                });
                            }
                        }
                    }
                }
                return NotFound(new { message = "Không tìm thấy người dùng" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi Server: " + ex.Message });
            }
        }
    }
}