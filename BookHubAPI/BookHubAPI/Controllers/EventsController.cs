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

        private string GetConnectionString() => _configuration.GetConnectionString("DefaultConnection");

        // 1. LẤY TẤT CẢ SỰ KIỆN (Dùng cho Trang chủ)
        [HttpGet]
        public IActionResult GetAllEvents()
        {
            var events = new List<object>();
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    string sql = "SELECT event_id, title, description, start_date, end_date, image_banner_url FROM Events WHERE is_active = 1";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                string fileName = reader["image_banner_url"] != DBNull.Value ? reader["image_banner_url"].ToString() : "";
                                string fullUrl = (!string.IsNullOrEmpty(fileName) && !fileName.StartsWith("http"))
                                    ? $"{Request.Scheme}://{Request.Host}/images/{fileName}"
                                    : fileName;

                                events.Add(new
                                {
                                    id = Convert.ToInt32(reader["event_id"]),
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
            catch (Exception ex) { return StatusCode(500, "Lỗi: " + ex.Message); }
        }

        // 2. LẤY SỰ KIỆN "CỦA TÔI" (Những sự kiện user đã đăng ký)
        [HttpGet("my-events")]
        public IActionResult GetMyEvents(int userId)
        {
            var events = new List<object>();
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    // Join bảng Events và EventRegistrations để lấy sự kiện user đã tham gia
                    string sql = @"
                        SELECT e.event_id, e.title, e.description, e.start_date, e.end_date, e.image_banner_url 
                        FROM Events e
                        JOIN EventRegistrations er ON e.event_id = er.event_id
                        WHERE er.user_id = @uid";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", userId);
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                string fileName = reader["image_banner_url"] != DBNull.Value ? reader["image_banner_url"].ToString() : "";
                                string fullUrl = (!string.IsNullOrEmpty(fileName) && !fileName.StartsWith("http"))
                                    ? $"{Request.Scheme}://{Request.Host}/images/{fileName}"
                                    : fileName;

                                events.Add(new
                                {
                                    id = Convert.ToInt32(reader["event_id"]),
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
            catch (Exception ex) { return StatusCode(500, "Lỗi: " + ex.Message); }
        }

        // 3. ĐĂNG KÝ THAM GIA
        [HttpPost("register")]
        public IActionResult RegisterEvent([FromBody] RegistrationRequest req)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    // Check trùng
                    string checkSql = "SELECT COUNT(*) FROM EventRegistrations WHERE user_id = @uid AND event_id = @eid";
                    using (SqlCommand checkCmd = new SqlCommand(checkSql, conn))
                    {
                        checkCmd.Parameters.AddWithValue("@uid", req.UserId);
                        checkCmd.Parameters.AddWithValue("@eid", req.EventId);
                        if ((int)checkCmd.ExecuteScalar() > 0) return BadRequest(new { message = "Bạn đã đăng ký rồi!" });
                    }

                    // Insert
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
            catch (Exception ex) { return StatusCode(500, new { message = "Lỗi: " + ex.Message }); }
        }

        // 4. HỦY ĐĂNG KÝ (ĐÂY LÀ API BẠN ĐANG THIẾU)
        [HttpPost("cancel")]
        public IActionResult CancelRegistration([FromBody] RegistrationRequest req)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    string sql = "DELETE FROM EventRegistrations WHERE user_id = @uid AND event_id = @eid";
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", req.UserId);
                        cmd.Parameters.AddWithValue("@eid", req.EventId);
                        int rows = cmd.ExecuteNonQuery();

                        if (rows > 0) return Ok(new { message = "Đã hủy đăng ký!" });
                        else return BadRequest(new { message = "Bạn chưa đăng ký sự kiện này." });
                    }
                }
            }
            catch (Exception ex) { return StatusCode(500, new { message = "Lỗi: " + ex.Message }); }
        }

        // 5. KIỂM TRA TRẠNG THÁI (Để hiện nút đúng màu)
        [HttpGet("check-status")]
        public IActionResult CheckRegistration(int userId, int eventId)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    string sql = "SELECT COUNT(*) FROM EventRegistrations WHERE user_id = @uid AND event_id = @eid";
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", userId);
                        cmd.Parameters.AddWithValue("@eid", eventId);
                        return Ok(new { isRegistered = (int)cmd.ExecuteScalar() > 0 });
                    }
                }
            }
            catch (Exception ex) { return StatusCode(500, new { message = "Lỗi: " + ex.Message }); }
        }
    }

    public class RegistrationRequest
    {
        public int UserId { get; set; }
        public int EventId { get; set; }
    }
}