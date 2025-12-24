using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using System.Data;

namespace BookHubAPI.Controllers
{
    [Route("api/books")]
    [ApiController]
    public class BooksController : ControllerBase
    {
        // Thay đổi Server=... bằng tên máy của bạn nếu cần
        private readonly string connectionString = "Server=ADMIN-PC,1433;Database=bookhub_db;User Id=sa;Password=123456;TrustServerCertificate=True;";

        // API: Lấy tất cả sách (GET: api/books)
        [HttpGet]
        public IActionResult GetAllBooks()
        {
            List<object> books = new List<object>();

            try
            {
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();
                    // Lấy dữ liệu và map tên cột cho khớp với class Book trong Android
                    string sql = "SELECT book_id, title, author, average_rating as rating, page_count as pages, current_status as status, price, review_count as reviews, description FROM Books";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                books.Add(new
                                {
                                    id = reader["book_id"],
                                    title = reader["title"],
                                    author = reader["author"],
                                    rating = reader["rating"],
                                    pages = reader["pages"],
                                    status = reader["status"],
                                    price = reader["price"] + " VND", // Format giá
                                    reviews = reader["reviews"],
                                    description = reader["description"],
                                    // Tạm thời để ảnh rỗng, sau này bạn update URL ảnh thật vào DB
                                    imageUrl = ""
                                });
                            }
                        }
                    }
                }
                return Ok(books);
            }
            catch (Exception ex)
            {
                return StatusCode(500, "Lỗi: " + ex.Message);
            }
        }
    }
}