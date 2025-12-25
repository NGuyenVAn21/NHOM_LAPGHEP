using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BookHubAPI.Models
{
    [Table("BorrowRecords")]
    public class BorrowRecord
    {
        [Key]
        [Column("record_id")]
        public int RecordId { get; set; }

        [Column("user_id")]
        public int UserId { get; set; }

        [Column("book_id")]
        public int BookId { get; set; }

        [Column("borrow_date")]
        public DateTime BorrowDate { get; set; } = DateTime.Now;

        [Column("due_date")]
        public DateTime DueDate { get; set; }

        [Column("return_date")]
        public DateTime? ReturnDate { get; set; }

        [Column("status")]
        public string Status { get; set; } = "Borrowing";

        public User? User { get; set; }
        public Book? Book { get; set; }
        [Column("image_file")]  // ✅ Đổi từ cover_image_url sang image_file
        public string? CoverImageUrl { get; set; }
    }
}