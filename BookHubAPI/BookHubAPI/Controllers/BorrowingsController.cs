using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using System.Collections.Generic;
using System;
using Microsoft.Extensions.Configuration;

namespace BookHubAPI.Controllers
{
    [Route("api/borrowings")]
    [ApiController]
    public class BorrowingsController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public BorrowingsController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        private string GetConnectionString()
        {
            return _configuration.GetConnectionString("DefaultConnection");
        }

        // ==================== GET ALL BORROWINGS (with filter) ====================
        [HttpGet]
        public IActionResult GetBorrowings([FromQuery] string status = null)
        {
            var borrowings = new List<object>();
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // Base query - ✅ FIX: Dùng cover_image_url thay vì image_file
                    string sql = @"
                        SELECT br.record_id, br.user_id, br.book_id, br.borrow_date, 
                               br.due_date, br.return_date, br.status,
                               u.full_name, u.email, u.avatar_url,
                               b.title, b.author, b.cover_image_url
                        FROM BorrowRecords br
                        JOIN Users u ON br.user_id = u.user_id
                        JOIN Books b ON br.book_id = b.book_id
                    ";

                    // Filter by status
                    if (!string.IsNullOrEmpty(status))
                    {
                        sql += " WHERE br.status = @status";
                    }

                    sql += " ORDER BY br.borrow_date DESC";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        if (!string.IsNullOrEmpty(status))
                        {
                            cmd.Parameters.AddWithValue("@status", status);
                        }

                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                // Process avatar
                                string avatarUrl = reader["avatar_url"] != DBNull.Value ? reader["avatar_url"].ToString() : "";
                                if (!string.IsNullOrEmpty(avatarUrl) && !avatarUrl.StartsWith("http"))
                                {
                                    avatarUrl = $"{Request.Scheme}://{Request.Host}/images/{avatarUrl}";
                                }

                                // Process book cover - ✅ FIX: Dùng cover_image_url
                                string coverUrl = reader["cover_image_url"] != DBNull.Value ? reader["cover_image_url"].ToString() : "";
                                if (!string.IsNullOrEmpty(coverUrl) && !coverUrl.StartsWith("http"))
                                {
                                    coverUrl = $"{Request.Scheme}://{Request.Host}/images/{coverUrl}";
                                }

                                borrowings.Add(new
                                {
                                    recordId = Convert.ToInt32(reader["record_id"]),
                                    userId = Convert.ToInt32(reader["user_id"]),
                                    bookId = Convert.ToInt32(reader["book_id"]),
                                    borrowDate = Convert.ToDateTime(reader["borrow_date"]),
                                    dueDate = Convert.ToDateTime(reader["due_date"]),
                                    returnDate = reader["return_date"] != DBNull.Value ?
                                        Convert.ToDateTime(reader["return_date"]) : (DateTime?)null,
                                    status = reader["status"].ToString(),
                                    user = new
                                    {
                                        fullName = reader["full_name"].ToString(),
                                        email = reader["email"].ToString(),
                                        avatarUrl = avatarUrl
                                    },
                                    book = new
                                    {
                                        title = reader["title"].ToString(),
                                        author = reader["author"].ToString(),
                                        coverUrl = coverUrl
                                    }
                                });
                            }
                        }
                    }
                }
                return Ok(borrowings);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi Server: " + ex.Message });
            }
        }

        // ==================== GET BORROWING BY ID ====================
        [HttpGet("{id}")]
        public IActionResult GetBorrowingById(int id)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    string sql = @"
                        SELECT br.*, u.full_name, u.email, b.title, b.author 
                        FROM BorrowRecords br
                        JOIN Users u ON br.user_id = u.user_id
                        JOIN Books b ON br.book_id = b.book_id
                        WHERE br.record_id = @id
                    ";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            if (reader.Read())
                            {
                                return Ok(new
                                {
                                    recordId = Convert.ToInt32(reader["record_id"]),
                                    userId = Convert.ToInt32(reader["user_id"]),
                                    bookId = Convert.ToInt32(reader["book_id"]),
                                    borrowDate = Convert.ToDateTime(reader["borrow_date"]),
                                    dueDate = Convert.ToDateTime(reader["due_date"]),
                                    returnDate = reader["return_date"] != DBNull.Value ?
                                        Convert.ToDateTime(reader["return_date"]) : (DateTime?)null,
                                    status = reader["status"].ToString(),
                                    userName = reader["full_name"].ToString(),
                                    userEmail = reader["email"].ToString(),
                                    bookTitle = reader["title"].ToString(),
                                    bookAuthor = reader["author"].ToString()
                                });
                            }
                        }
                    }
                }
                return NotFound(new { message = "Không tìm thấy bản ghi mượn sách" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi Server: " + ex.Message });
            }
        }

        // ==================== CREATE BORROWING (Admin tạo lượt mượn) ====================
        [HttpPost]
        public IActionResult CreateBorrowing([FromBody] BorrowingDto dto)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // Kiểm tra sách còn hàng không
                    string checkSql = "SELECT stock_quantity FROM Books WHERE book_id = @bookId";
                    using (SqlCommand checkCmd = new SqlCommand(checkSql, conn))
                    {
                        checkCmd.Parameters.AddWithValue("@bookId", dto.BookId);
                        object result = checkCmd.ExecuteScalar();
                        if (result == null)
                            return NotFound(new { success = false, message = "Không tìm thấy sách" });

                        int stock = Convert.ToInt32(result);
                        if (stock <= 0)
                            return BadRequest(new { success = false, message = "Sách đã hết hàng" });
                    }

                    string sql = @"
                        INSERT INTO BorrowRecords (user_id, book_id, borrow_date, due_date, status)
                        VALUES (@userId, @bookId, GETDATE(), @dueDate, 'Borrowing')
                    ";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@userId", dto.UserId);
                        cmd.Parameters.AddWithValue("@bookId", dto.BookId);
                        cmd.Parameters.AddWithValue("@dueDate", dto.DueDate ?? DateTime.Now.AddDays(14));
                        cmd.ExecuteNonQuery();

                        return Ok(new { success = true, message = "Tạo lượt mượn thành công!" });
                    }
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = "Lỗi: " + ex.Message });
            }
        }

        // ==================== RETURN BOOK ====================
        [HttpPut("{id}/return")]
        public IActionResult ReturnBook(int id)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // Kiểm tra record có tồn tại và đang mượn không
                    string checkSql = "SELECT status FROM BorrowRecords WHERE record_id = @id";
                    using (SqlCommand checkCmd = new SqlCommand(checkSql, conn))
                    {
                        checkCmd.Parameters.AddWithValue("@id", id);
                        object result = checkCmd.ExecuteScalar();
                        if (result == null)
                            return NotFound(new { success = false, message = "Không tìm thấy bản ghi" });

                        string currentStatus = result.ToString();
                        if (currentStatus != "Borrowing" && currentStatus != "Overdue")
                            return BadRequest(new { success = false, message = "Sách không trong trạng thái mượn" });
                    }

                    string sql = @"
                        UPDATE BorrowRecords 
                        SET status = 'Returned', return_date = GETDATE() 
                        WHERE record_id = @id
                    ";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        cmd.ExecuteNonQuery();
                        return Ok(new { success = true, message = "Trả sách thành công!" });
                    }
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = "Lỗi: " + ex.Message });
            }
        }

        // ==================== EXTEND DUE DATE ====================
        [HttpPut("{id}/extend")]
        public IActionResult ExtendDueDate(int id, [FromBody] ExtendDto dto)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    string sql = @"
                        UPDATE BorrowRecords 
                        SET due_date = DATEADD(day, @days, due_date) 
                        WHERE record_id = @id AND status = 'Borrowing'
                    ";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        cmd.Parameters.AddWithValue("@days", dto.Days ?? 7);
                        int rows = cmd.ExecuteNonQuery();

                        if (rows > 0)
                            return Ok(new { success = true, message = $"Gia hạn thêm {dto.Days ?? 7} ngày!" });
                        else
                            return BadRequest(new { success = false, message = "Không thể gia hạn" });
                    }
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = "Lỗi: " + ex.Message });
            }
        }

        // ==================== DELETE BORROWING ====================
        [HttpDelete("{id}")]
        public IActionResult DeleteBorrowing(int id)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // Chỉ cho phép xóa nếu đã trả hoặc đã hủy
                    string checkSql = "SELECT status FROM BorrowRecords WHERE record_id = @id";
                    using (SqlCommand checkCmd = new SqlCommand(checkSql, conn))
                    {
                        checkCmd.Parameters.AddWithValue("@id", id);
                        object result = checkCmd.ExecuteScalar();
                        if (result == null)
                            return NotFound(new { success = false, message = "Không tìm thấy bản ghi" });

                        string status = result.ToString();
                        if (status == "Borrowing" || status == "Overdue")
                            return BadRequest(new
                            {
                                success = false,
                                message = "Không thể xóa lượt mượn đang hoạt động. Vui lòng trả sách trước!"
                            });
                    }

                    string sql = "DELETE FROM BorrowRecords WHERE record_id = @id";
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        cmd.ExecuteNonQuery();
                        return Ok(new { success = true, message = "Xóa thành công!" });
                    }
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = "Lỗi: " + ex.Message });
            }
        }
    }

    // DTO Classes
    public class BorrowingDto
    {
        public int UserId { get; set; }
        public int BookId { get; set; }
        public DateTime? DueDate { get; set; }
    }

    public class ExtendDto
    {
        public int? Days { get; set; } = 7;
    }
}