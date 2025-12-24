using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using Microsoft.Extensions.Configuration;
using System.Collections.Generic;
using System;

namespace BookHubAPI.Controllers
{
    [Route("api/stats")]
    [ApiController]
    public class StatsController : ControllerBase
    {
        private readonly IConfiguration _configuration;
        public StatsController(IConfiguration configuration) { _configuration = configuration; }

        [HttpGet("active-readers")]
        public IActionResult GetActiveReaders()
        {
            var users = new List<object>();
            try
            {
                string connectionString = _configuration.GetConnectionString("DefaultConnection");
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();
                    // Query: Đếm số sách đã mượn (kể cả đã trả) và xếp giảm dần
                    string sql = @"
                        SELECT TOP 5 u.user_id, u.full_name, u.avatar_url, COUNT(br.record_id) as total_borrow
                        FROM Users u
                        JOIN BorrowRecords br ON u.user_id = br.user_id
                        GROUP BY u.user_id, u.full_name, u.avatar_url
                        ORDER BY total_borrow DESC";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                // Xử lý ảnh avatar
                                string fileName = reader["avatar_url"] != DBNull.Value ? reader["avatar_url"].ToString() : "";
                                string fullUrl = "";
                                if (!string.IsNullOrEmpty(fileName) && !fileName.StartsWith("http"))
                                {
                                    fullUrl = $"{Request.Scheme}://{Request.Host}/images/{fileName}";
                                }
                                else fullUrl = fileName;

                                users.Add(new
                                {
                                    id = Convert.ToInt32(reader["user_id"]),
                                    name = reader["full_name"].ToString(),
                                    avatar = fullUrl,
                                    borrowCount = Convert.ToInt32(reader["total_borrow"])
                                });
                            }
                        }
                    }
                }
                return Ok(users);
            }
            catch (Exception ex) { return StatusCode(500, "Lỗi: " + ex.Message); }
        }
    }
}