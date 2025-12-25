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
        // 1. API LẤY DANH SÁCH (GIỮ NGUYÊN)
        // =============================================================
        [HttpGet("current")]
        public IActionResult GetCurrentBorrows(int userId)
        {
            return GetBorrowRecordsByStatus(userId, "Borrowing");
        }

        [HttpGet("history")]
        public IActionResult GetHistory(int userId)
        {
            return GetBorrowRecordsByStatus(userId, "Returned");
        }

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
                    // 1. SỬA SQL: Thêm b.price vào SELECT
                    string sql = @"
                        SELECT br.record_id, br.borrow_date, br.due_date, br.return_date, br.status,
                               b.book_id, b.title, b.author, b.image_file, b.price
                        FROM BorrowRecords br
                        JOIN Books b ON br.book_id = b.book_id
                        WHERE br.user_id = @uid AND br.status = @status
                        ORDER BY br.borrow_date DESC";

                    if (status == "Borrowing")
                    {
                        // 1. SỬA SQL: Thêm b.price
                        sql = @"
                        SELECT br.record_id, br.borrow_date, br.due_date, br.return_date, br.status,
                               b.book_id, b.title, b.author, b.image_file, b.price
                        FROM BorrowRecords br
                        JOIN Books b ON br.book_id = b.book_id
                        WHERE br.user_id = @uid AND (br.status = 'Borrowing' OR br.status = 'Overdue')
                        ORDER BY br.due_date ASC";
                    }

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", userId);
                        cmd.Parameters.AddWithValue("@status", status);

                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                string img = reader["image_file"] != DBNull.Value ? reader["image_file"].ToString() : "";
                                if (!string.IsNullOrEmpty(img) && !img.StartsWith("http"))
                                    img = $"{Request.Scheme}://{Request.Host}/images/{img}";

                                // 2. XỬ LÝ GIÁ TIỀN (Format sang VND)
                                string priceStr = (reader["price"] != DBNull.Value ? Convert.ToDecimal(reader["price"]).ToString("N0") : "0") + " VND";

                                string dbStatus = reader["status"].ToString();
                                DateTime dueDate = Convert.ToDateTime(reader["due_date"]);
                                string displayStatus = "";
                                string statusColor = "";

                                if (dbStatus == "Returned") { displayStatus = "Đã trả"; statusColor = "#EF5350"; }
                                else if (dbStatus == "Reserved") { displayStatus = "Sẵn sàng"; statusColor = "#66BB6A"; }
                                else
                                {
                                    if (dueDate < DateTime.Now) { displayStatus = "Quá hạn"; statusColor = "#D32F2F"; }
                                    else { displayStatus = "Đang mượn"; statusColor = "#AB47BC"; }
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
                                    status = dbStatus,
                                    displayStatus = displayStatus,
                                    statusColor = statusColor,

                                    // 3. TRẢ VỀ GIÁ TIỀN
                                    price = priceStr
                                });
                            }
                        }
                    }
                }
                return Ok(list);
            }
            catch (Exception ex) { return StatusCode(500, new { message = "Lỗi Server: " + ex.Message }); }
        }

        // =============================================================
        // 2. API MỚI: TẠO YÊU CẦU MƯỢN SÁCH (QUAN TRỌNG)
        // =============================================================
        [HttpPost("create")]
        public IActionResult BorrowBook([FromBody] BorrowRequest req)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // Bước 1: Kiểm tra xem sách còn trong kho không
                    string checkStockSql = "SELECT stock_quantity FROM Books WHERE book_id = @bid";
                    using (SqlCommand cmdCheck = new SqlCommand(checkStockSql, conn))
                    {
                        cmdCheck.Parameters.AddWithValue("@bid", req.BookId);
                        object result = cmdCheck.ExecuteScalar();

                        if (result == null) return NotFound(new { success = false, message = "Sách không tồn tại." });

                        int stock = Convert.ToInt32(result);
                        if (stock <= 0)
                        {
                            return BadRequest(new { success = false, message = "Sách này hiện đã hết hàng!" });
                        }
                    }

                    // Bước 2: Kiểm tra xem user này có đang mượn cuốn này chưa trả không (tránh spam)
                    string checkDupSql = "SELECT COUNT(*) FROM BorrowRecords WHERE user_id = @uid AND book_id = @bid AND status IN ('Borrowing', 'Overdue')";
                    using (SqlCommand cmdDup = new SqlCommand(checkDupSql, conn))
                    {
                        cmdDup.Parameters.AddWithValue("@uid", req.UserId);
                        cmdDup.Parameters.AddWithValue("@bid", req.BookId);
                        int count = (int)cmdDup.ExecuteScalar();
                        if (count > 0)
                        {
                            return BadRequest(new { success = false, message = "Bạn đang mượn cuốn sách này rồi!" });
                        }
                    }

                    // Bước 3: Insert vào BorrowRecords -> Trigger trg_BorrowBook sẽ tự động trừ kho
                    string sql = @"INSERT INTO BorrowRecords (user_id, book_id, borrow_date, due_date, status) 
                                   VALUES (@uid, @bid, GETDATE(), DATEADD(day, 14, GETDATE()), 'Borrowing')";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", req.UserId);
                        cmd.Parameters.AddWithValue("@bid", req.BookId);
                        cmd.ExecuteNonQuery();
                    }
                }
                return Ok(new { success = true, message = "Mượn sách thành công! Hạn trả là 14 ngày." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = ex.Message });
            }
        }

        // =============================================================
        // 3. CÁC API HÀNH ĐỘNG KHÁC (GIỮ NGUYÊN)
        // =============================================================

        [HttpPost("return")]
        public IActionResult ReturnBook([FromBody] ActionRequest req)
        {

            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
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

        [HttpPost("extend")]
        public IActionResult ExtendBook([FromBody] ActionRequest req)
        {
            // ... (Code cũ giữ nguyên)
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
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

        [HttpPost("cancel")]
        public IActionResult CancelReservation([FromBody] ActionRequest req)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
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

    // Các class DTO
    public class ActionRequest
    {
        public int RecordId { get; set; }
        public int UserId { get; set; }
    }

    // Class mới cho API BorrowBook
    public class BorrowRequest
    {
        public int UserId { get; set; }
        public int BookId { get; set; }
    }
}