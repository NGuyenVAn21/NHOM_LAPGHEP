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

        private readonly string connectionString = "Server=ADMIN-PC,1433;Database=bookhub_db;User Id=sa;Password=123456;TrustServerCertificate=True;";

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
    }
}