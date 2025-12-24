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

        [HttpGet]
        public IActionResult GetAllBooks()
        {
            var books = new List<object>();
            try
            {
                string connectionString = _configuration.GetConnectionString("DefaultConnection");

                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();

                    // Sửa câu query: Lấy cột image_file
                    string sql = @"
                        SELECT b.book_id, b.title, b.author, b.average_rating, b.page_count, 
                               b.current_status, b.price, b.review_count, b.description, 
                               b.image_file, 
                               c.category_name
                        FROM Books b
                        LEFT JOIN Categories c ON b.category_id = c.category_id";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                // XỬ LÝ ẢNH: Tự động ghép tên miền server vào tên file
                                string fileName = reader["image_file"] != DBNull.Value ? reader["image_file"].ToString() : "";
                                string fullImageUrl = "";

                                if (!string.IsNullOrEmpty(fileName))
                                {
                                    // Kết quả sẽ là: http://10.0.2.2:5177/images/nha_gia_kim.jpg
                                    fullImageUrl = $"{Request.Scheme}://{Request.Host}/images/{fileName}";
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

                                    // Trả về link đầy đủ cho App Android dùng luôn
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