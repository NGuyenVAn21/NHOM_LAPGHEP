using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using Microsoft.Extensions.Configuration;
using System.Collections.Generic;
using System;

namespace BookHubAPI.Controllers
{
    [Route("api/borrow")]
    [ApiController]
    public class BorrowController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public BorrowController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        private string GetConnectionString()
        {
            return _configuration.GetConnectionString("DefaultConnection");
        }

        // =============================================================
        // 1. API LẤY DANH SÁCH CHO 3 TAB
        // =============================================================

        // GET: api/borrow/current?userId=1
        // Phục vụ Tab "Đang mượn"
        [HttpGet("current")]
        public IActionResult GetCurrentBorrows(int userId)
        {
            return GetBorrowRecordsByStatus(userId, "Borrowing");
        }

        // GET: api/borrow/history?userId=1
        // Phục vụ Tab "Lịch sử"
        [HttpGet("history")]
        public IActionResult GetHistory(int userId)
        {
            return GetBorrowRecordsByStatus(userId, "Returned");
        }

        // GET: api/borrow/reservations?userId=1
        // Phục vụ Tab "Đặt trước"
        [HttpGet("reservations")]
        public IActionResult GetReservations(int userId)
        {
            return GetBorrowRecordsByStatus(userId, "Reserved");
        }

        // Hàm chung để query dữ liệu cho gọn
        private IActionResult GetBorrowRecordsByStatus(int userId, string status)
        {
            var list = new List<object>();
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    // Query kết hợp bảng BorrowRecords và Books
                    string sql = @"
                        SELECT br.record_id, br.borrow_date, br.due_date, br.return_date, br.status,
                               b.book_id, b.title, b.author, b.cover_image_url
                        FROM BorrowRecords br
                        JOIN Books b ON br.book_id = b.book_id
                        WHERE br.user_id = @uid AND br.status = @status
                        ORDER BY br.borrow_date DESC"; // Mới nhất lên đầu

                    // Nếu là tab Đang mượn (Borrowing), ta cần lấy cả những sách đã Quá hạn (Overdue) để hiển thị cảnh báo
                    if (status == "Borrowing")
                    {
                        sql = @"
                        SELECT br.record_id, br.borrow_date, br.due_date, br.return_date, br.status,
                               b.book_id, b.title, b.author, b.cover_image_url
                        FROM BorrowRecords br
                        JOIN Books b ON br.book_id = b.book_id
                        WHERE br.user_id = @uid AND (br.status = 'Borrowing' OR br.status = 'Overdue')
                        ORDER BY br.due_date ASC"; // Sắp xếp theo hạn trả (gần hết hạn lên đầu)
                    }

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", userId);
                        cmd.Parameters.AddWithValue("@status", status);

                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                // Xử lý link ảnh
                                string img = reader["cover_image_url"] != DBNull.Value ? reader["cover_image_url"].ToString() : "";
                                if (!string.IsNullOrEmpty(img) && !img.StartsWith("http"))
                                    img = $"{Request.Scheme}://{Request.Host}/images/{img}";

                                // Tính toán trạng thái hiển thị cho UI
                                string dbStatus = reader["status"].ToString();
                                DateTime dueDate = Convert.ToDateTime(reader["due_date"]);
                                string displayStatus = "";
                                string statusColor = ""; // Gợi ý màu cho App (hoặc App tự xử lý)

                                if (dbStatus == "Returned")
                                {
                                    displayStatus = "Đã trả";
                                    statusColor = "#EF5350"; // Đỏ nhạt
                                }
                                else if (dbStatus == "Reserved")
                                {
                                    displayStatus = "Sẵn sàng"; // Hoặc "Chờ sách" tùy logic kho
                                    statusColor = "#66BB6A"; // Xanh
                                }
                                else // Borrowing hoặc Overdue
                                {
                                    if (dueDate < DateTime.Now)
                                    {
                                        displayStatus = "Quá hạn";
                                        statusColor = "#D32F2F"; // Đỏ đậm
                                    }
                                    else
                                    {
                                        displayStatus = "Đang mượn";
                                        statusColor = "#AB47BC"; // Tím
                                    }
                                }

                                list.Add(new
                                {
                                    recordId = reader["record_id"],
                                    bookId = reader["book_id"],
                                    title = reader["title"],
                                    author = reader["author"],
                                    coverUrl = img,
                                    borrowDate = Convert.ToDateTime(reader["borrow_date"]).ToString("dd/MM/yyyy"),
                                    dueDate = dueDate.ToString("dd/MM/yyyy"),
                                    returnDate = reader["return_date"] != DBNull.Value ? Convert.ToDateTime(reader["return_date"]).ToString("dd/MM/yyyy") : null,
                                    status = dbStatus,       // Status gốc trong DB (để logic code)
                                    displayStatus = displayStatus, // Text hiển thị lên App
                                    statusColor = statusColor
                                });
                            }
                        }
                    }
                }
                return Ok(list);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi Server: " + ex.Message });
            }
        }

        // =============================================================
        // 2. CÁC API HÀNH ĐỘNG (BUTTONS)
        // =============================================================

        // POST: api/borrow/return
        // Nút "Trả sách"
        [HttpPost("return")]
        public IActionResult ReturnBook([FromBody] ActionRequest req)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    // Update ngày trả và đổi status -> Trigger SQL sẽ tự cộng tồn kho
                    string sql = @"UPDATE BorrowRecords 
                                   SET status = 'Returned', return_date = GETDATE() 
                                   WHERE record_id = @rid AND user_id = @uid AND (status = 'Borrowing' OR status = 'Overdue')";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@rid", req.RecordId);
                        cmd.Parameters.AddWithValue("@uid", req.UserId);
                        int rows = cmd.ExecuteNonQuery();

                        if (rows > 0) return Ok(new { success = true, message = "Trả sách thành công!" });
                        else return BadRequest(new { success = false, message = "Lỗi: Không tìm thấy lượt mượn hoặc sách đã trả." });
                    }
                }
            }
            catch (Exception ex) { return StatusCode(500, new { message = ex.Message }); }
        }

        // POST: api/borrow/extend
        // Nút "Gia hạn" (Cộng thêm 7 ngày)
        [HttpPost("extend")]
        public IActionResult ExtendBook([FromBody] ActionRequest req)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    // Logic: Chỉ cho gia hạn nếu đang mượn và chưa quá hạn (tùy bạn chọn)
                    // Ở đây tôi cho phép gia hạn kể cả khi quá hạn, nó sẽ cộng thêm từ ngày due_date cũ
                    string sql = @"UPDATE BorrowRecords 
                                   SET due_date = DATEADD(day, 7, due_date) 
                                   WHERE record_id = @rid AND user_id = @uid AND status = 'Borrowing'";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@rid", req.RecordId);
                        cmd.Parameters.AddWithValue("@uid", req.UserId);
                        int rows = cmd.ExecuteNonQuery();

                        if (rows > 0) return Ok(new { success = true, message = "Gia hạn thêm 7 ngày thành công!" });
                        else return BadRequest(new { success = false, message = "Không thể gia hạn (Sách đã trả hoặc quá hạn)." });
                    }
                }
            }
            catch (Exception ex) { return StatusCode(500, new { message = ex.Message }); }
        }

        // POST: api/borrow/cancel
        // Nút "Hủy đặt" (Tab Đặt trước)
        [HttpPost("cancel")]
        public IActionResult CancelReservation([FromBody] ActionRequest req)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    // Chuyển status sang Cancelled hoặc xóa luôn dòng đó tùy nghiệp vụ
                    // Ở đây tôi xóa luôn cho sạch Data hoặc update status = 'Cancelled'
                    string sql = "UPDATE BorrowRecords SET status = 'Cancelled' WHERE record_id = @rid AND user_id = @uid AND status = 'Reserved'";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@rid", req.RecordId);
                        cmd.Parameters.AddWithValue("@uid", req.UserId);
                        int rows = cmd.ExecuteNonQuery();

                        if (rows > 0) return Ok(new { success = true, message = "Đã hủy đặt trước." });
                        else return BadRequest(new { success = false, message = "Lỗi thao tác." });
                    }
                }
            }
            catch (Exception ex) { return StatusCode(500, new { message = ex.Message }); }
        }
    }

    // Class nhận dữ liệu từ Client gửi lên
    public class ActionRequest
    {
        public int RecordId { get; set; } // ID của lượt mượn (BorrowRecord)
        public int UserId { get; set; }   // ID user để bảo mật (tránh trả hộ người khác)
    }
}