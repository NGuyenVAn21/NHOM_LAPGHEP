using BookHubAPI.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BookHubAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class BorrowingsController : ControllerBase
    {
        private readonly AppDbContext _context;

        public BorrowingsController(AppDbContext context)
        {
            _context = context;
        }

        // ✅ GET: api/borrowings?status=Borrowing (HỖ TRỢ QUERY PARAM)
        [HttpGet]
        public async Task<ActionResult<IEnumerable<BorrowDto>>> GetBorrowings([FromQuery] string? status = null)
        {
            var query = _context.BorrowRecords
                .Include(b => b.User)
                .Include(b => b.Book)
                .AsQueryable();

            // Lọc theo status nếu có
            if (!string.IsNullOrEmpty(status))
            {
                query = query.Where(b => b.Status == status);
            }

            var result = await query
                .OrderByDescending(b => b.BorrowDate)
                .Select(b => new BorrowDto
                {
                    RecordId = b.RecordId,
                    UserId = b.UserId,
                    BookId = b.BookId,
                    BorrowDate = b.BorrowDate,
                    DueDate = b.DueDate,
                    ReturnDate = b.ReturnDate,
                    Status = b.Status,
                    User = new UserSimpleDto
                    {
                        UserId = b.User!.UserId,
                        FullName = b.User.FullName,
                        Username = b.User.Username
                    },
                    Book = new BookSimpleDto
                    {
                        BookId = b.Book!.BookId,
                        Title = b.Book.Title,
                        Author = b.Book.Author,
                        CoverImageUrl = b.Book.CoverImageUrl
                    }
                })
                .ToListAsync();

            return Ok(result);
        }

        // GET: api/borrowings/5
        [HttpGet("{id}")]
        public async Task<ActionResult<BorrowDto>> GetBorrowing(int id)
        {
            var borrow = await _context.BorrowRecords
                .Include(b => b.User)
                .Include(b => b.Book)
                .FirstOrDefaultAsync(b => b.RecordId == id);

            if (borrow == null)
                return NotFound();

            return Ok(new BorrowDto
            {
                RecordId = borrow.RecordId,
                UserId = borrow.UserId,
                BookId = borrow.BookId,
                BorrowDate = borrow.BorrowDate,
                DueDate = borrow.DueDate,
                ReturnDate = borrow.ReturnDate,
                Status = borrow.Status,
                User = new UserSimpleDto
                {
                    UserId = borrow.User!.UserId,
                    FullName = borrow.User.FullName,
                    Username = borrow.User.Username
                },
                Book = new BookSimpleDto
                {
                    BookId = borrow.Book!.BookId,
                    Title = borrow.Book.Title,
                    Author = borrow.Book.Author,
                    CoverImageUrl = borrow.Book.CoverImageUrl
                }
            });
        }

        // POST: api/borrowings
        [HttpPost]
        public async Task<ActionResult> BorrowBook([FromBody] BorrowRequest request)
        {
            var book = await _context.Books.FindAsync(request.BookId);
            if (book == null || book.StockQuantity <= 0)
                return BadRequest(new { message = "Sách không tồn tại hoặc đã hết hàng" });

            var user = await _context.Users.FindAsync(request.UserId);
            if (user == null)
                return BadRequest(new { message = "Người dùng không tồn tại" });

            var borrowing = new BorrowRecord
            {
                UserId = request.UserId,
                BookId = request.BookId,
                BorrowDate = DateTime.Now,
                DueDate = DateTime.Now.AddDays(14),
                Status = "Borrowing"
            };

            _context.BorrowRecords.Add(borrowing);

            // Trigger sẽ tự động giảm stock, nhưng vẫn cập nhật để đảm bảo
            book.StockQuantity--;
            if (book.StockQuantity <= 0)
                book.CurrentStatus = "Hết hàng";

            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetBorrowing), new { id = borrowing.RecordId }, borrowing);
        }

        // ✅ PUT: api/borrowings/5/return
        [HttpPut("{id}/return")]
        public async Task<ActionResult> ReturnBook(int id)
        {
            var borrowing = await _context.BorrowRecords
                .Include(b => b.Book)
                .FirstOrDefaultAsync(b => b.RecordId == id);

            if (borrowing == null)
                return NotFound(new { message = "Không tìm thấy bản ghi mượn" });

            if (borrowing.Status != "Borrowing")
                return BadRequest(new { message = "Sách đã được trả hoặc trạng thái không hợp lệ" });

            borrowing.Status = "Returned";
            borrowing.ReturnDate = DateTime.Now;

            // Trigger sẽ tự động tăng stock, nhưng vẫn cập nhật để đảm bảo
            if (borrowing.Book != null)
            {
                borrowing.Book.StockQuantity++;
                if (borrowing.Book.StockQuantity > 0)
                    borrowing.Book.CurrentStatus = "Có sẵn";
            }

            await _context.SaveChangesAsync();
            return Ok(new { message = "Trả sách thành công", borrowing });
        }

        // DELETE: api/borrowings/5
        [HttpDelete("{id}")]
        public async Task<ActionResult> DeleteBorrowing(int id)
        {
            var borrowing = await _context.BorrowRecords.FindAsync(id);
            if (borrowing == null)
                return NotFound();

            _context.BorrowRecords.Remove(borrowing);
            await _context.SaveChangesAsync();
            return NoContent();
        }
    }

    // DTOs
    public class BorrowDto
    {
        public int RecordId { get; set; }
        public int UserId { get; set; }
        public int BookId { get; set; }
        public DateTime BorrowDate { get; set; }
        public DateTime DueDate { get; set; }
        public DateTime? ReturnDate { get; set; }
        public string Status { get; set; } = string.Empty;
        public UserSimpleDto? User { get; set; }
        public BookSimpleDto? Book { get; set; }
    }

    public class UserSimpleDto
    {
        public int UserId { get; set; }
        public string FullName { get; set; } = string.Empty;
        public string Username { get; set; } = string.Empty;
    }

    public class BookSimpleDto
    {
        public int BookId { get; set; }
        public string Title { get; set; } = string.Empty;
        public string Author { get; set; } = string.Empty;
        public string? CoverImageUrl { get; set; }
    }

    public class BorrowRequest
    {
        public int UserId { get; set; }
        public int BookId { get; set; }
    }
}