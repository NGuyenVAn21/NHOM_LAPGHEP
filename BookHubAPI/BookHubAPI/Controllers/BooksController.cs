using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using System.Collections.Generic;
using System;

namespace BookHubAPI.Controllers
{
    [Route("api/books")]
    [ApiController]
    public class BooksController : ControllerBase
    {

        private readonly string connectionString = "Server=MSI\\SQLEXPRESS,1433;Database=bookhub_db;User Id=sa;Password=12345;TrustServerCertificate=True;";

        [HttpGet]
        public IActionResult GetAllBooks()
        {
            var books = new List<object>();
            try
            {
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();

 
                    string sql = @"
                        SELECT b.book_id, b.title, b.author, b.average_rating, b.page_count, 
                               b.current_status, b.price, b.review_count, b.description, 
                               b.cover_image_url, 
                               c.category_name
                        FROM Books b
                        LEFT JOIN Categories c ON b.category_id = c.category_id";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
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
									imageUrl = reader["cover_image_url"] != DBNull.Value ? reader["cover_image_url"].ToString() : "" 
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
		[HttpGet("{id}")]
		public IActionResult GetBookById(int id)
		{
			try
			{
				using (SqlConnection conn = new SqlConnection(connectionString))
				{
					conn.Open();

					string sql = @"
                SELECT b.book_id, b.title, b.author, b.average_rating, b.page_count, 
                       b.current_status, b.price, b.review_count, b.description, 
                       b.cover_image_url, b.publisher, b.published_year, b.stock_quantity,
                       c.category_name
                FROM Books b
                LEFT JOIN Categories c ON b.category_id = c.category_id
                WHERE b.book_id = @id";

					using (SqlCommand cmd = new SqlCommand(sql, conn))
					{
						cmd.Parameters.AddWithValue("@id", id);

						using (SqlDataReader reader = cmd.ExecuteReader())
						{
							if (reader.Read())
							{
								var book = new
								{
									id = Convert.ToInt32(reader["book_id"]),
									title = reader["title"].ToString(),
									author = reader["author"].ToString(),
									publisher = reader["publisher"] != DBNull.Value ? reader["publisher"].ToString() : "Chưa có",
									year = reader["published_year"] != DBNull.Value ? Convert.ToInt32(reader["published_year"]) : 0, // ĐỔI TÊN: year thay vì publishedYear
									pages = reader["page_count"] != DBNull.Value ? Convert.ToInt32(reader["page_count"]) : 0, // ĐỔI TÊN: pages thay vì pageCount
									description = reader["description"].ToString(),
									price = (reader["price"] != DBNull.Value ? Convert.ToDecimal(reader["price"]).ToString("N0") : "0") + " VND",
									rating = reader["average_rating"] != DBNull.Value ? Convert.ToSingle(reader["average_rating"]) : 0f,
									reviews = reader["review_count"] != DBNull.Value ? Convert.ToInt32(reader["review_count"]) : 0, // ĐỔI TÊN: reviews thay vì reviewCount
									status = reader["current_status"].ToString(),
									stock = reader["stock_quantity"] != DBNull.Value ? Convert.ToInt32(reader["stock_quantity"]) : 0,
									category = reader["category_name"] != DBNull.Value ? reader["category_name"].ToString() : "Tổng hợp",
									imageUrl = reader["cover_image_url"] != DBNull.Value ? reader["cover_image_url"].ToString() : ""
								};

								return Ok(book);
							}
							else
							{
								return NotFound(new { message = "Không tìm thấy sách" });
							}
						}
					}
				}
			}
			catch (Exception ex)
			{
				return StatusCode(500, new { message = "Lỗi server: " + ex.Message });
			}
		}
		// API: Tìm kiếm sách theo từ khóa
		[HttpGet("search")]
		public IActionResult SearchBooks([FromQuery] string keyword)
		{
			var books = new List<object>();
			try
			{
				using (SqlConnection conn = new SqlConnection(connectionString))
				{
					conn.Open();

					string sql = @"
                SELECT b.book_id, b.title, b.author, b.average_rating, 
                       b.page_count, b.current_status, b.price, b.cover_image_url,
                       c.category_name
                FROM Books b
                LEFT JOIN Categories c ON b.category_id = c.category_id
                WHERE b.title LIKE @keyword OR b.author LIKE @keyword
                ORDER BY b.title";

					using (SqlCommand cmd = new SqlCommand(sql, conn))
					{
						cmd.Parameters.AddWithValue("@keyword", "%" + keyword + "%");

						using (SqlDataReader reader = cmd.ExecuteReader())
						{
							while (reader.Read())
							{
								books.Add(new
								{
									id = Convert.ToInt32(reader["book_id"]),
									title = reader["title"].ToString(),
									author = reader["author"].ToString(),
									rating = reader["average_rating"] != DBNull.Value ? Convert.ToSingle(reader["average_rating"]) : 0f,
									pages = reader["page_count"] != DBNull.Value ? Convert.ToInt32(reader["page_count"]) : 0, // ĐỔI: pages
									status = reader["current_status"].ToString(),
									price = (reader["price"] != DBNull.Value ? Convert.ToDecimal(reader["price"]).ToString("N0") : "0") + " VND",
									category = reader["category_name"] != DBNull.Value ? reader["category_name"].ToString() : "Tổng hợp",
									imageUrl = reader["cover_image_url"] != DBNull.Value ? reader["cover_image_url"].ToString() : "" // ĐỔI: imageUrl
								});
							}
						}
					}
				}
				return Ok(books);
			}
			catch (Exception ex)
			{
				return StatusCode(500, new { message = "Lỗi server: " + ex.Message });
			}
		}
	}
}