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

 
        // 1. API LẤY DANH SÁCH (GIỮ NGUYÊN)
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

        // Hàm chung để query dữ liệu
        private IActionResult GetBorrowRecordsByStatus(int userId, string status)
        {
            var list = new List<object>();
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    // SQL mặc định
                    string sql = @"
                        SELECT br.record_id, br.borrow_date, br.due_date, br.return_date, br.status,
                               b.book_id, b.title, b.author, b.image_file, b.price
                        FROM BorrowRecords br
                        JOIN Books b ON br.book_id = b.book_id
                        WHERE br.user_id = @uid AND br.status = @status
                        ORDER BY br.borrow_date DESC";

                    if (status == "Borrowing")
                    {
                        // Lấy cả Borrowing và Overdue
                        sql = @"
                        SELECT br.record_id, br.borrow_date, br.due_date, br.return_date, br.status,
                               b.book_id, b.title, b.author, b.image_file, b.price
                        FROM BorrowRecords br
                        JOIN Books b ON br.book_id = b.book_id
                        WHERE br.user_id = @uid AND (br.status = 'Borrowing' OR br.status = 'Overdue')
                        ORDER BY br.due_date ASC";
                    }
                    else if (status == "Reserved")
                    {
                        // Lấy cả Reserved (Đang chờ) và Ready (Đã có sách)
                        sql = @"
                        SELECT br.record_id, br.borrow_date, br.due_date, br.return_date, br.status,
                               b.book_id, b.title, b.author, b.image_file, b.price
                        FROM BorrowRecords br
                        JOIN Books b ON br.book_id = b.book_id
                        WHERE br.user_id = @uid AND (br.status = 'Reserved' OR br.status = 'Ready')
                        ORDER BY br.borrow_date DESC";
                    }

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", userId);
                        // Chỉ add parameter status nếu không phải trường hợp đặc biệt đã hardcode trong SQL
                        if (status != "Borrowing" && status != "Reserved")
                        {
                            cmd.Parameters.AddWithValue("@status", status);
                        }

                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                string img = reader["image_file"] != DBNull.Value ? reader["image_file"].ToString() : "";
                                if (!string.IsNullOrEmpty(img) && !img.StartsWith("http"))
                                    img = $"{Request.Scheme}://{Request.Host}/images/{img}";

                                string priceStr = (reader["price"] != DBNull.Value ? Convert.ToDecimal(reader["price"]).ToString("N0") : "0") + " VND";

                                string dbStatus = reader["status"].ToString();
                                DateTime dueDate = Convert.ToDateTime(reader["due_date"]);
                                string displayStatus = "";
                                string statusColor = "";

                                if (dbStatus == "Returned") { displayStatus = "Đã trả"; statusColor = "#EF5350"; }
                                else if (dbStatus == "Reserved") { displayStatus = "Đang chờ"; statusColor = "#FF9800"; }
                                else if (dbStatus == "Ready") { displayStatus = "Sẵn sàng"; statusColor = "#4CAF50"; }
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


        // 2. API MƯỢN SÁCH
 
        [HttpPost("create")]
        public IActionResult BorrowBook([FromBody] BorrowRequest req)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // --- CHECK 1: Giới hạn mỗi người chỉ được mượn tối đa 5 cuốn ---
                    string limitSql = "SELECT COUNT(*) FROM BorrowRecords WHERE user_id = @uid AND status IN ('Borrowing', 'Overdue', 'Ready')";
                    using (SqlCommand cmd = new SqlCommand(limitSql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", req.UserId);
                        int count = (int)cmd.ExecuteScalar();
                        if (count >= 5) return BadRequest(new { success = false, message = "Bạn chỉ được mượn tối đa 5 cuốn cùng lúc!" });
                    }

                    // --- CHECK 2: Kiểm tra xem User này có đang được giữ chỗ (Ready) không? ---
                    bool isClaimingReservation = false;
                    string checkReadySql = "SELECT COUNT(*) FROM BorrowRecords WHERE user_id = @uid AND book_id = @bid AND status = 'Ready'";
                    using (SqlCommand cmd = new SqlCommand(checkReadySql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", req.UserId);
                        cmd.Parameters.AddWithValue("@bid", req.BookId);
                        if ((int)cmd.ExecuteScalar() > 0) isClaimingReservation = true;
                    }

                    // --- CHECK 3: Kiểm tra tồn kho (Nếu KHÔNG phải là người được giữ chỗ) ---
                    if (!isClaimingReservation)
                    {
                        string stockSql = "SELECT stock_quantity FROM Books WHERE book_id = @bid";
                        using (SqlCommand cmd = new SqlCommand(stockSql, conn))
                        {
                            cmd.Parameters.AddWithValue("@bid", req.BookId);
                            object result = cmd.ExecuteScalar();
                            if (result == null || Convert.ToInt32(result) <= 0)
                                return BadRequest(new { success = false, message = "Sách đã hết hàng, vui lòng Đặt trước!" });
                        }
                    }

                    // --- CHECK 4: Tránh mượn trùng (Nếu đang Ready thì cho qua để update) ---
                    if (!isClaimingReservation)
                    {
                        string dupSql = "SELECT COUNT(*) FROM BorrowRecords WHERE user_id = @uid AND book_id = @bid AND status IN ('Borrowing', 'Overdue')";
                        using (SqlCommand cmd = new SqlCommand(dupSql, conn))
                        {
                            cmd.Parameters.AddWithValue("@uid", req.UserId);
                            cmd.Parameters.AddWithValue("@bid", req.BookId);
                            if ((int)cmd.ExecuteScalar() > 0) return BadRequest(new { success = false, message = "Bạn đang giữ cuốn sách này rồi." });
                        }
                    }

                    // --- THỰC HIỆN MƯỢN ---
                    if (isClaimingReservation)
                    {
                        // Nếu đang Ready (đã xếp hàng thành công) -> Update thành Borrowing (Không cần trừ kho vì kho đã giữ cho người này)
                        string sqlUpdate = "UPDATE BorrowRecords SET status = 'Borrowing', borrow_date = GETDATE(), due_date = DATEADD(day, 14, GETDATE()) WHERE user_id = @uid AND book_id = @bid AND status = 'Ready'";
                        using (SqlCommand cmd = new SqlCommand(sqlUpdate, conn))
                        {
                            cmd.Parameters.AddWithValue("@uid", req.UserId);
                            cmd.Parameters.AddWithValue("@bid", req.BookId);
                            cmd.ExecuteNonQuery();
                        }
                    }
                    else
                    {
                        // Mượn mới hoàn toàn -> Insert và Trừ kho
                        string sqlInsert = "INSERT INTO BorrowRecords (user_id, book_id, borrow_date, due_date, status) VALUES (@uid, @bid, GETDATE(), DATEADD(day, 14, GETDATE()), 'Borrowing')";
                        using (SqlCommand cmd = new SqlCommand(sqlInsert, conn))
                        {
                            cmd.Parameters.AddWithValue("@uid", req.UserId);
                            cmd.Parameters.AddWithValue("@bid", req.BookId);
                            cmd.ExecuteNonQuery();
                        }

                        // Trừ kho
                        string sqlStock = "UPDATE Books SET stock_quantity = stock_quantity - 1 WHERE book_id = @bid";
                        using (SqlCommand cmd = new SqlCommand(sqlStock, conn))
                        {
                            cmd.Parameters.AddWithValue("@bid", req.BookId);
                            cmd.ExecuteNonQuery();
                        }

                        // Cập nhật trạng thái hiển thị nếu kho về 0
                        string sqlStatus = "UPDATE Books SET current_status = N'Hết hàng' WHERE book_id = @bid AND stock_quantity <= 0";
                        using (SqlCommand cmd = new SqlCommand(sqlStatus, conn))
                        {
                            cmd.Parameters.AddWithValue("@bid", req.BookId);
                            cmd.ExecuteNonQuery();
                        }
                    }
                }
                return Ok(new { success = true, message = "Mượn sách thành công! Hạn trả là 14 ngày." });
            }
            catch (Exception ex) { return StatusCode(500, new { success = false, message = ex.Message }); }
        }
        // 3. API ĐẶT TRƯỚC (RESERVE)
 
        [HttpPost("reserve")]
        public IActionResult ReserveBook([FromBody] BorrowRequest req)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // Check: Chỉ cho đặt nếu sách HẾT hàng
                    string stockSql = "SELECT stock_quantity FROM Books WHERE book_id = @bid";
                    using (SqlCommand cmd = new SqlCommand(stockSql, conn))
                    {
                        cmd.Parameters.AddWithValue("@bid", req.BookId);
                        int stock = (int)cmd.ExecuteScalar();
                        if (stock > 0) return BadRequest(new { success = false, message = "Sách đang có sẵn, bạn hãy bấm Mượn sách!" });
                    }

                    // Check: Đã đặt trước đó chưa?
                    string dupSql = "SELECT COUNT(*) FROM BorrowRecords WHERE user_id = @uid AND book_id = @bid AND status IN ('Reserved', 'Ready')";
                    using (SqlCommand cmd = new SqlCommand(dupSql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", req.UserId);
                        cmd.Parameters.AddWithValue("@bid", req.BookId);
                        if ((int)cmd.ExecuteScalar() > 0) return BadRequest(new { success = false, message = "Bạn đã đặt trước cuốn này rồi." });
                    }

                    // Insert Reserved
                    string sql = "INSERT INTO BorrowRecords (user_id, book_id, borrow_date, due_date, status) VALUES (@uid, @bid, GETDATE(), GETDATE(), 'Reserved')";
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@uid", req.UserId);
                        cmd.Parameters.AddWithValue("@bid", req.BookId);
                        cmd.ExecuteNonQuery();
                    }
                }
                return Ok(new { success = true, message = "Đã vào danh sách chờ! Chúng tôi sẽ báo khi có sách." });
            }
            catch (Exception ex) { return StatusCode(500, new { success = false, message = ex.Message }); }
        }

        // 4. API HÀNH ĐỘNG KHÁC (GIỮ NGUYÊN)


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

    public class BorrowRequest
    {
        public int UserId { get; set; }
        public int BookId { get; set; }
    }
}