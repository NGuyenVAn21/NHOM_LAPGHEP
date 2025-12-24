using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using System.Data;

namespace BookHubAPI.Controllers
{
	[Route("api/reading")]
	[ApiController]
	public class ReadingController : ControllerBase
	{
		private readonly string connectionString = "Server=MSI\\SQLEXPRESS;Database=bookhub_db;User Id=sa;Password=12345;TrustServerCertificate=True;";

		[HttpGet("chapters/{bookId}")]
		public IActionResult GetChapters(int bookId)
		{
			var chapters = new List<object>();
			try
			{
				using (SqlConnection conn = new SqlConnection(connectionString))
				{
					conn.Open();

					string sql = "SELECT chapter_id, chapter_num, title FROM Chapters WHERE book_id = @bookId ORDER BY chapter_num";

					using (SqlCommand cmd = new SqlCommand(sql, conn))
					{
						cmd.Parameters.AddWithValue("@bookId", bookId);

						using (SqlDataReader reader = cmd.ExecuteReader())
						{
							while (reader.Read())
							{
								chapters.Add(new
								{
									chapterId = Convert.ToInt32(reader["chapter_id"]), // Đúng: chapterId
									chapterNum = Convert.ToInt32(reader["chapter_num"]), // Đúng: chapterNum
									title = reader["title"].ToString() // Đúng: title
								});
							}
						}
					}
				}
				return Ok(chapters);
			}
			catch (Exception ex)
			{
				return StatusCode(500, new { message = "Lỗi server: " + ex.Message });
			}
		}
		// API: Lấy nội dung 1 chapter cụ thể
		[HttpGet("chapter/{chapterId}")]
		public IActionResult GetChapterContent(int chapterId)
		{
			try
			{
				using (SqlConnection conn = new SqlConnection(connectionString))
				{
					conn.Open();

					string sql = "SELECT chapter_id, chapter_num, title, content FROM Chapters WHERE chapter_id = @chapterId";

					using (SqlCommand cmd = new SqlCommand(sql, conn))
					{
						cmd.Parameters.AddWithValue("@chapterId", chapterId);

						using (SqlDataReader reader = cmd.ExecuteReader())
						{
							if (reader.Read())
							{
								var chapter = new
								{
									chapterId = Convert.ToInt32(reader["chapter_id"]), // THÊM DÒNG NÀY
									chapterNum = Convert.ToInt32(reader["chapter_num"]),
									title = reader["title"].ToString(),
									content = reader["content"].ToString() // ĐẢM BẢO CÓ CONTENT
								};
								return Ok(chapter);
							}
							else
							{
								return NotFound(new { message = "Không tìm thấy chapter" });
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
	}
}