using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BookHubAPI.Models
{
    [Table("Books")]
    public class Book
    {
        [Key]
        [Column("book_id")]
        public int BookId { get; set; }

        [Required, MaxLength(255)]
        [Column("title")]
        public string Title { get; set; } = string.Empty;

        [Required, MaxLength(255)]
        [Column("author")]
        public string Author { get; set; } = string.Empty;

        [MaxLength(255)]
        [Column("publisher")]
        public string? Publisher { get; set; }

        [Column("published_year")]
        public int? PublishedYear { get; set; }

        [Column("page_count")]
        public int? PageCount { get; set; }

        [Column("description")]
        public string? Description { get; set; }

        [Column("price")]
        public decimal Price { get; set; } = 0;
        [Column("image_file")]  // ✅ Đổi từ cover_image_url sang image_file
        public string? CoverImageUrl { get; set; }


        [Column("category_id")]
        public int? CategoryId { get; set; }

        [Column("stock_quantity")]
        public int StockQuantity { get; set; } = 1;

        [Column("current_status")]
        public string CurrentStatus { get; set; } = "Có sẵn";

        // ✅ FIX: Đổi từ float sang double để khớp với SQL Server FLOAT
        [Column("average_rating")]
        public double AverageRating { get; set; } = 0;

        [Column("review_count")]
        public int ReviewCount { get; set; } = 0;

        [Column("created_at")]
        public DateTime CreatedAt { get; set; } = DateTime.Now;

        public Category? Category { get; set; }
    }
}