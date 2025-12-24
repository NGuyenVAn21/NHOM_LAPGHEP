using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using Microsoft.Extensions.Configuration;
using System.Collections.Generic;
using System;

namespace BookHubAPI.Controllers
{
    [Route("api/events")]
    [ApiController]
    public class EventsController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public EventsController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        // 1. API LẤY DANH SÁCH (Giữ nguyên logic cũ của bạn)
        [HttpGet]
        public IActionResult GetAllEvents()
        {
            var events = new List<object>();
            try
            {
                string connectionString = _configuration.GetConnectionString("DefaultConnection");
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();
                    string sql = "SELECT event_id, title, description, start_date, end_date, image_banner_url FROM Events WHERE is_active = 1";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                // Xử lý link ảnh
                                string fileName = reader["image_banner_url"] != DBNull.Value ? reader["image_banner_url"].ToString() : "";
                                string fullUrl = "";
                                if (!string.IsNullOrEmpty(fileName) && !fileName.StartsWith("http"))
                                {
                                    fullUrl = $"{Request.Scheme}://{Request.Host}/images/{fileName}";
                                }
                                else
                                {
                                    fullUrl = fileName;
                                }

                                events.Add(new
                                {
                                    id = Convert.ToInt32(reader["event_id"]), // QUAN TRỌNG: Trả về ID
                                    title = reader["title"].ToString(),
                                    description = reader["description"].ToString(),
                                    startDate = Convert.ToDateTime(reader["start_date"]).ToString("dd/MM/yyyy"),
                                    endDate = Convert.ToDateTime(reader["end_date"]).ToString("dd/MM/yyyy"),
                                    imageUrl = fullUrl
                                });
                            }
                        }
                    }
                }
                return Ok(events);
            }
            catch (Exception ex)
            {
                return StatusCode(500, "Lỗi Server: " + ex.Message);
            }
        }

        // 2. API ĐĂNG KÝ (Thêm mới đoạn này)
        [HttpPost("register")]
        public IActionResult RegisterEvent([FromBody] RegistrationRequest req)
        {
            try
            {
                string connectionString = _configuration.GetConnectionString("DefaultConnection");
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();

                    // Kiểm tra xem đã đăng ký chưa
                    string checkSql = "SELECT COUNT(*) FROM EventRegistrations WHERE user_id = @uid AND event_id = @eid";
                    using (SqlCommand checkCmd = new SqlCommand(checkSql, conn))
                    {
                        checkCmd.Parameters.AddWithValue("@uid", req.UserId);
                        checkCmd.Parameters.AddWithValue("@eid", req.EventId);
                        int count = (int)checkCmd.ExecuteScalar();
                        if (count > 0) return BadRequest(new { message = "Bạn đã đăng ký sự kiện này rồi!" });
                    }

                    // Nếu chưa thì Insert
                    string sql = "INSERT INTO EventRegistrations (user_id, event_id, status) VALUES (@uid, @eid, N'Đã đăng ký')";
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", req.UserId);
                        cmd.Parameters.AddWithValue("@eid", req.EventId);
                        cmd.ExecuteNonQuery();
                    }
                }
                return Ok(new { message = "Đăng ký thành công!" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi Server: " + ex.Message });
            }
        }
        // ... (Các hàm cũ giữ nguyên) ...

        // 3. API KIỂM TRA TRẠNG THÁI ĐĂNG KÝ
        [HttpGet("check-status")]
        public IActionResult CheckRegistration(int userId, int eventId)
        {
            try
            {
                string connectionString = _configuration.GetConnectionString("DefaultConnection");
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();
                    string sql = "SELECT COUNT(*) FROM EventRegistrations WHERE user_id = @uid AND event_id = @eid";
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", userId);
                        cmd.Parameters.AddWithValue("@eid", eventId);
                        int count = (int)cmd.ExecuteScalar();

                        // Trả về true nếu count > 0 (đã đăng ký)
                        return Ok(new { isRegistered = count > 0 });
                    }
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi: " + ex.Message });
            }
        }
    }

    // Class hứng dữ liệu
    public class RegistrationRequest
    {
        public int UserId { get; set; }
        public int EventId { get; set; }
    }

}