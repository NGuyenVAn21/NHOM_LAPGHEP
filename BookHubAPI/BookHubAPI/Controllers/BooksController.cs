using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using System.Collections.Generic;
using System;
using Microsoft.Extensions.Configuration;

namespace BookHubAPI.Controllers
{
    [Route("api/books")]
    [ApiController]
    public class BooksController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public BooksController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        private string GetConnectionString()
        {
            return _configuration.GetConnectionString("DefaultConnection");
        }

        // ==================== GET ALL BOOKS ====================
        [HttpGet]
        public IActionResult GetAllBooks()
        {
            return ExecuteQuery(@"
                SELECT b.*, c.category_name 
                FROM Books b 
                LEFT JOIN Categories c ON b.category_id = c.category_id
                ORDER BY b.book_id DESC
            ");
        }

        // ==================== GET BOOK BY ID ====================
        [HttpGet("{id}")]
        public IActionResult GetBookById(int id)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    string sql = @"
                        SELECT b.*, c.category_name 
                        FROM Books b 
                        LEFT JOIN Categories c ON b.category_id = c.category_id
                        WHERE b.book_id = @id
                    ";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            if (reader.Read())
                            {
                                return Ok(CreateBookObject(reader));
                            }
                        }
                    }
                }
                return NotFound(new { message = "Không tìm thấy sách" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi Server: " + ex.Message });
            }
        }

        // ==================== NEW BOOKS ====================
        [HttpGet("new")]
        public IActionResult GetNewBooks()
        {
            return ExecuteQuery(@"
                SELECT TOP 6 b.*, c.category_name 
                FROM Books b 
                LEFT JOIN Categories c ON b.category_id = c.category_id 
                ORDER BY b.book_id DESC
            ");
        }

        // ==================== POPULAR BOOKS ====================
        [HttpGet("popular")]
        public IActionResult GetPopularBooks()
        {
            try
            {
                var books = new List<object>();
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // ✅ Query đơn giản hóa - không JOIN với BorrowRecords để tránh lỗi
                    string sql = @"
                        SELECT TOP 6 b.*, c.category_name 
                        FROM Books b
                        LEFT JOIN Categories c ON b.category_id = c.category_id
                        ORDER BY b.book_id DESC
                    ";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                books.Add(CreateBookObject(reader));
                            }
                        }
                    }
                }
                return Ok(books);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi Server: " + ex.Message });
            }
        }

        // ==================== CREATE BOOK ====================
        [HttpPost]
        public IActionResult CreateBook([FromBody] BookDto book)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // ✅ Kiểm tra xem có cột nào trong Books table
                    string checkColumnSql = @"
                        SELECT COLUMN_NAME 
                        FROM INFORMATION_SCHEMA.COLUMNS 
                        WHERE TABLE_NAME = 'Books' 
                        AND COLUMN_NAME IN ('image_file', 'cover_image_url')
                    ";

                    string imageColumnName = "cover_image_url"; // default
                    using (SqlCommand checkCmd = new SqlCommand(checkColumnSql, conn))
                    {
                        using (SqlDataReader reader = checkCmd.ExecuteReader())
                        {
                            if (reader.Read())
                            {
                                imageColumnName = reader["COLUMN_NAME"].ToString();
                            }
                        }
                    }

                    string sql = $@"
                        INSERT INTO Books (title, author, publisher, published_year, page_count, 
                                         description, price, {imageColumnName}, category_id, stock_quantity, current_status)
                        VALUES (@title, @author, @publisher, @year, @pages, 
                                @desc, @price, @img, @catId, @stock, @status);
                        SELECT CAST(SCOPE_IDENTITY() as int);
                    ";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@title", book.Title ?? "");
                        cmd.Parameters.AddWithValue("@author", book.Author ?? "");
                        cmd.Parameters.AddWithValue("@publisher", book.Publisher ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@year", book.PublishedYear ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@pages", book.PageCount ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@desc", book.Description ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@price", book.Price);
                        cmd.Parameters.AddWithValue("@img", book.CoverImageUrl ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@catId", book.CategoryId ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@stock", book.StockQuantity);
                        cmd.Parameters.AddWithValue("@status", book.StockQuantity > 0 ? "Có sẵn" : "Hết hàng");

                        int newId = (int)cmd.ExecuteScalar();
                        return Ok(new { success = true, message = "Thêm sách thành công!", id = newId });
                    }
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = "Lỗi: " + ex.Message });
            }
        }

        // ==================== UPDATE BOOK ====================
        [HttpPut("{id}")]
        public IActionResult UpdateBook(int id, [FromBody] BookDto book)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // Kiểm tra sách có tồn tại không
                    string checkSql = "SELECT COUNT(*) FROM Books WHERE book_id = @id";
                    using (SqlCommand checkCmd = new SqlCommand(checkSql, conn))
                    {
                        checkCmd.Parameters.AddWithValue("@id", id);
                        int count = (int)checkCmd.ExecuteScalar();
                        if (count == 0)
                            return NotFound(new { success = false, message = "Không tìm thấy sách" });
                    }

                    // ✅ Kiểm tra tên cột image
                    string checkColumnSql = @"
                        SELECT COLUMN_NAME 
                        FROM INFORMATION_SCHEMA.COLUMNS 
                        WHERE TABLE_NAME = 'Books' 
                        AND COLUMN_NAME IN ('image_file', 'cover_image_url')
                    ";

                    string imageColumnName = "cover_image_url"; // default
                    using (SqlCommand checkCmd = new SqlCommand(checkColumnSql, conn))
                    {
                        using (SqlDataReader reader = checkCmd.ExecuteReader())
                        {
                            if (reader.Read())
                            {
                                imageColumnName = reader["COLUMN_NAME"].ToString();
                            }
                        }
                    }

                    string sql = $@"
                        UPDATE Books 
                        SET title = @title, 
                            author = @author, 
                            publisher = @publisher,
                            published_year = @year,
                            page_count = @pages,
                            description = @desc, 
                            price = @price, 
                            {imageColumnName} = @img, 
                            category_id = @catId, 
                            stock_quantity = @stock,
                            current_status = @status
                        WHERE book_id = @id
                    ";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        cmd.Parameters.AddWithValue("@title", book.Title ?? "");
                        cmd.Parameters.AddWithValue("@author", book.Author ?? "");
                        cmd.Parameters.AddWithValue("@publisher", book.Publisher ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@year", book.PublishedYear ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@pages", book.PageCount ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@desc", book.Description ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@price", book.Price);
                        cmd.Parameters.AddWithValue("@img", book.CoverImageUrl ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@catId", book.CategoryId ?? (object)DBNull.Value);
                        cmd.Parameters.AddWithValue("@stock", book.StockQuantity);
                        cmd.Parameters.AddWithValue("@status", book.StockQuantity > 0 ? "Có sẵn" : "Hết hàng");

                        cmd.ExecuteNonQuery();
                        return Ok(new { success = true, message = "Cập nhật thành công!" });
                    }
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = "Lỗi: " + ex.Message });
            }
        }

        // ==================== DELETE BOOK ====================
        [HttpDelete("{id}")]
        public IActionResult DeleteBook(int id)
        {
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();

                    // Kiểm tra xem có lượt mượn nào đang active không
                    string checkSql = @"
                        SELECT COUNT(*) FROM BorrowRecords 
                        WHERE book_id = @id AND status IN ('Borrowing', 'Reserved')
                    ";
                    using (SqlCommand checkCmd = new SqlCommand(checkSql, conn))
                    {
                        checkCmd.Parameters.AddWithValue("@id", id);
                        int count = (int)checkCmd.ExecuteScalar();
                        if (count > 0)
                            return BadRequest(new
                            {
                                success = false,
                                message = "Không thể xóa sách đang được mượn hoặc đặt trước!"
                            });
                    }

                    string sql = "DELETE FROM Books WHERE book_id = @id";
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        int rows = cmd.ExecuteNonQuery();

                        if (rows > 0)
                            return Ok(new { success = true, message = "Xóa thành công!" });
                        else
                            return NotFound(new { success = false, message = "Không tìm thấy sách" });
                    }
                }
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { success = false, message = "Lỗi: " + ex.Message });
            }
        }

        // ==================== HELPER FUNCTIONS ====================
        private IActionResult ExecuteQuery(string sql)
        {
            var books = new List<object>();
            try
            {
                using (SqlConnection conn = new SqlConnection(GetConnectionString()))
                {
                    conn.Open();
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                books.Add(CreateBookObject(reader));
                            }
                        }
                    }
                }
                return Ok(books);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Lỗi Server: " + ex.Message });
            }
        }

        private object CreateBookObject(SqlDataReader reader)
        {
            // ✅ Tự động phát hiện tên cột image
            string fileName = "";
            try
            {
                // Thử image_file trước
                if (HasColumn(reader, "image_file"))
                {
                    fileName = reader["image_file"] != DBNull.Value ? reader["image_file"].ToString() : "";
                }
                // Nếu không có thì thử cover_image_url
                else if (HasColumn(reader, "cover_image_url"))
                {
                    fileName = reader["cover_image_url"] != DBNull.Value ? reader["cover_image_url"].ToString() : "";
                }
            }
            catch
            {
                fileName = "";
            }

            string fullImageUrl = "";
            if (!string.IsNullOrEmpty(fileName))
            {
                if (!fileName.StartsWith("http"))
                    fullImageUrl = $"{Request.Scheme}://{Request.Host}/images/{fileName}";
                else
                    fullImageUrl = fileName;
            }

            return new
            {
                id = Convert.ToInt32(reader["book_id"]),
                title = reader["title"].ToString(),
                author = reader["author"].ToString(),
                publisher = reader["publisher"] != DBNull.Value ? reader["publisher"].ToString() : null,
                publishedYear = reader["published_year"] != DBNull.Value ? Convert.ToInt32(reader["published_year"]) : (int?)null,
                rating = reader["average_rating"] != DBNull.Value ? Convert.ToDouble(reader["average_rating"]) : 0.0,
                pages = reader["page_count"] != DBNull.Value ? Convert.ToInt32(reader["page_count"]) : 0,
                status = reader["current_status"].ToString(),
                stock = reader["stock_quantity"] != DBNull.Value ? Convert.ToInt32(reader["stock_quantity"]) : 0,
                price = (reader["price"] != DBNull.Value ? Convert.ToDecimal(reader["price"]).ToString("N0") : "0") + " VND",
                reviews = reader["review_count"] != DBNull.Value ? Convert.ToInt32(reader["review_count"]) : 0,
                description = reader["description"] != DBNull.Value ? reader["description"].ToString() : "",
                category = reader["category_name"] != DBNull.Value ? reader["category_name"].ToString() : "Tổng hợp",
                categoryId = reader["category_id"] != DBNull.Value ? Convert.ToInt32(reader["category_id"]) : (int?)null,
                coverImageUrl = fullImageUrl
            };
        }

        // ✅ Helper method để kiểm tra column có tồn tại không
        private bool HasColumn(SqlDataReader reader, string columnName)
        {
            for (int i = 0; i < reader.FieldCount; i++)
            {
                if (reader.GetName(i).Equals(columnName, StringComparison.OrdinalIgnoreCase))
                    return true;
            }
            return false;
        }
    }

    // ==================== DTO CLASS ====================
    public class BookDto
    {
        public string Title { get; set; }
        public string Author { get; set; }
        public string Publisher { get; set; }
        public int? PublishedYear { get; set; }
        public int? PageCount { get; set; }
        public string Description { get; set; }
        public decimal Price { get; set; }
        public string CoverImageUrl { get; set; }
        public int? CategoryId { get; set; }
        public int StockQuantity { get; set; } = 1;
    }
}