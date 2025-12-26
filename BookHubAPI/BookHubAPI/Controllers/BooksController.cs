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

        // 1. API LẤY TẤT CẢ SÁCH 
        [HttpGet]
        public IActionResult GetAllBooks()
        {
            return ExecuteQuery("SELECT b.*, c.category_name FROM Books b LEFT JOIN Categories c ON b.category_id = c.category_id");
        }

        // 2. API SÁCH MỚI (Lấy theo ID giảm dần -> ID càng lớn là sách càng mới)
        [HttpGet("new")]
        public IActionResult GetNewBooks()
        {
            // Lấy 6 cuốn có ID lớn nhất
            string sql = @"
                SELECT TOP 6 b.*, c.category_name 
                FROM Books b 
                LEFT JOIN Categories c ON b.category_id = c.category_id 
                ORDER BY b.book_id DESC";

            return ExecuteQuery(sql);
        }

        // 3. API SÁCH PHỔ BIẾN (Đếm số lần xuất hiện trong bảng mượn)
        [HttpGet("popular")]
        public IActionResult GetPopularBooks()
        {
            // Đếm record_id trong bảng BorrowRecords để biết sách nào được mượn nhiều
            string sql = @"
                SELECT TOP 6 b.book_id, b.title, b.author, b.average_rating, b.page_count, 
                       b.current_status, b.price, b.review_count, b.description, 
                       b.image_file, c.category_name,
                       COUNT(br.record_id) as borrow_count
                FROM Books b
                LEFT JOIN Categories c ON b.category_id = c.category_id
                LEFT JOIN BorrowRecords br ON b.book_id = br.book_id
                GROUP BY b.book_id, b.title, b.author, b.average_rating, b.page_count, 
                         b.current_status, b.price, b.review_count, b.description, 
                         b.image_file, c.category_name
                ORDER BY borrow_count DESC";

            return ExecuteQuery(sql);
        }

        // 4. API LẤY NỘI DUNG SÁCH (CHƯƠNG)
        // GET: api/books/{id}/chapters
        [HttpGet("{id}/chapters")]
        public IActionResult GetBookChapters(int id)
        {
            var chapters = new List<object>();
            try
            {
                using (SqlConnection conn = new SqlConnection(_configuration.GetConnectionString("DefaultConnection")))
                {
                    conn.Open();
                    // Lấy các chương, sắp xếp theo thứ tự chapter_num
                    string sql = "SELECT chapter_id, chapter_num, title, content FROM Chapters WHERE book_id = @bid ORDER BY chapter_num ASC";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@bid", id);
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                chapters.Add(new
                                {
                                    chapterId = reader["chapter_id"],
                                    chapterNum = reader["chapter_num"],
                                    title = reader["title"].ToString(),
                                    content = reader["content"].ToString()
                                });
                            }
                        }
                    }
                }

                if (chapters.Count == 0)
                {
                    return NotFound(new { message = "Sách này chưa có nội dung." });
                }

                return Ok(chapters);
            }
            catch (Exception ex)
            {
                return StatusCode(500, "Lỗi Server: " + ex.Message);
            }
        }

        // HÀM DÙNG CHUNG
        private IActionResult ExecuteQuery(string sql)
        {
            var books = new List<object>();
            try
            {
                string connectionString = _configuration.GetConnectionString("DefaultConnection");
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();
                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                // XỬ LÝ ẢNH
                                string fileName = reader["image_file"] != DBNull.Value ? reader["image_file"].ToString() : "";
                                string fullImageUrl = "";
                                if (!string.IsNullOrEmpty(fileName))
                                {
                                    // Tự động ghép link nếu chưa có http
                                    if (!fileName.StartsWith("http"))
                                        fullImageUrl = $"{Request.Scheme}://{Request.Host}/images/{fileName}";
                                    else
                                        fullImageUrl = fileName;
                                }

                                books.Add(new
                                {
                                    id = Convert.ToInt32(reader["book_id"]),
                                    title = reader["title"].ToString(),
                                    author = reader["author"].ToString(),
                                    rating = reader["average_rating"] != DBNull.Value ? Convert.ToSingle(reader["average_rating"]) : 0f,
                                    pages = reader["page_count"] != DBNull.Value ? Convert.ToInt32(reader["page_count"]) : 0,
                                    status = reader["current_status"].ToString(),
                                    price = (reader["price"] != DBNull.Value ? Convert.ToDecimal(reader["price"]).ToString("N0") : "0") + " VND",
                                    reviews = reader["review_count"] != DBNull.Value ? Convert.ToInt32(reader["review_count"]) : 0,
                                    description = reader["description"].ToString(),
                                    category = reader["category_name"] != DBNull.Value ? reader["category_name"].ToString() : "Tổng hợp",
                                    imageUrl = fullImageUrl
                                });
                            }
                        }
                    }
                }
                return Ok(books);
            }
            catch (Exception ex)
            {
                return StatusCode(500, "Lỗi Server: " + ex.Message);
            }
        }
    }
}