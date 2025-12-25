using BookHubAPI.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BookHubAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class BooksController : ControllerBase
    {
        private readonly AppDbContext _context;

        public BooksController(AppDbContext context)
        {
            _context = context;
        }

        // GET: api/books
        [HttpGet]
        public async Task<ActionResult<IEnumerable<BookDto>>> GetBooks()
        {
            var books = await _context.Books
                .Include(b => b.Category)
                .Select(b => new BookDto
                {
                    Id = b.BookId,
                    Title = b.Title,
                    Author = b.Author,
                    Category = b.Category != null ? b.Category.CategoryName : "Khác",
                    CategoryId = b.CategoryId,
                    Price = b.Price,
                    CoverImageUrl = b.CoverImageUrl,
                    Rating = b.AverageRating,
                    Reviews = b.ReviewCount,
                    Status = b.CurrentStatus ?? "Có sẵn",
                    Stock = b.StockQuantity,
                    Description = b.Description
                })
                .ToListAsync();

            return Ok(books);
        }

        // GET: api/books/available
        [HttpGet("available")]
        public async Task<ActionResult<IEnumerable<BookDto>>> GetAvailableBooks()
        {
            var books = await _context.Books
                .Where(b => b.StockQuantity > 0)
                .Include(b => b.Category)
                .Select(b => new BookDto
                {
                    Id = b.BookId,
                    Title = b.Title,
                    Author = b.Author,
                    Category = b.Category != null ? b.Category.CategoryName : "Khác",
                    CategoryId = b.CategoryId,
                    Price = b.Price,
                    CoverImageUrl = b.CoverImageUrl,
                    Rating = b.AverageRating,
                    Reviews = b.ReviewCount,
                    Status = b.CurrentStatus ?? "Có sẵn",
                    Stock = b.StockQuantity
                })
                .ToListAsync();

            return Ok(books);
        }

        // GET: api/books/5
        [HttpGet("{id}")]
        public async Task<ActionResult<BookDetailDto>> GetBook(int id)
        {
            var book = await _context.Books
                .Include(b => b.Category)
                .FirstOrDefaultAsync(b => b.BookId == id);

            if (book == null)
                return NotFound();

            return Ok(new BookDetailDto
            {
                Id = book.BookId,
                Title = book.Title,
                Author = book.Author,
                Publisher = book.Publisher,
                PublishedYear = book.PublishedYear,
                PageCount = book.PageCount,
                Description = book.Description,
                Price = book.Price,
                CoverImageUrl = book.CoverImageUrl,
                Category = book.Category?.CategoryName ?? "Khác",
                CategoryId = book.CategoryId,
                Rating = book.AverageRating,
                Reviews = book.ReviewCount,
                Stock = book.StockQuantity,
                Status = book.CurrentStatus ?? "Có sẵn"
            });
        }

        // POST: api/books
        [HttpPost]
        public async Task<ActionResult> CreateBook([FromBody] CreateBookRequest request)
        {
            var book = new Book
            {
                Title = request.Title,
                Author = request.Author,
                CategoryId = request.CategoryId,
                Price = request.Price,
                CoverImageUrl = request.CoverImageUrl,
                Description = request.Description,
                StockQuantity = request.StockQuantity,
                CurrentStatus = request.StockQuantity > 0 ? "Có sẵn" : "Hết hàng"
            };

            _context.Books.Add(book);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetBook), new { id = book.BookId }, book);
        }

        // PUT: api/books/5
        [HttpPut("{id}")]
        public async Task<ActionResult> UpdateBook(int id, [FromBody] CreateBookRequest request)
        {
            var book = await _context.Books.FindAsync(id);
            if (book == null)
                return NotFound();

            book.Title = request.Title;
            book.Author = request.Author;
            book.CategoryId = request.CategoryId;
            book.Price = request.Price;
            book.Description = request.Description;
            book.StockQuantity = request.StockQuantity;
            book.CurrentStatus = request.StockQuantity > 0 ? "Có sẵn" : "Hết hàng";
            book.CoverImageUrl = request.CoverImageUrl;

            await _context.SaveChangesAsync();
            return NoContent();
        }

        // DELETE: api/books/5
        [HttpDelete("{id}")]
        public async Task<ActionResult> DeleteBook(int id)
        {
            var book = await _context.Books.FindAsync(id);
            if (book == null)
                return NotFound();

            _context.Books.Remove(book);
            await _context.SaveChangesAsync();
            return NoContent();
        }
    }

    // DTOs
    public class BookDto
    {
        public int Id { get; set; }
        public string Title { get; set; } = string.Empty;
        public string Author { get; set; } = string.Empty;
        public string Category { get; set; } = string.Empty;
        public int? CategoryId { get; set; }
        public decimal Price { get; set; }
        public string? CoverImageUrl { get; set; }

        // ✅ FIX: Đổi từ float sang double
        public double Rating { get; set; }

        public int Reviews { get; set; }
        public string Status { get; set; } = string.Empty;
        public int Stock { get; set; }
        public string? Description { get; set; }
    }

    public class BookDetailDto
    {
        public int Id { get; set; }
        public string Title { get; set; } = string.Empty;
        public string Author { get; set; } = string.Empty;
        public string? Publisher { get; set; }
        public int? PublishedYear { get; set; }
        public int? PageCount { get; set; }
        public string? Description { get; set; }
        public decimal Price { get; set; }
        public string? CoverImageUrl { get; set; }
        public string Category { get; set; } = string.Empty;
        public int? CategoryId { get; set; }

        // ✅ FIX: Đổi từ float sang double
        public double Rating { get; set; }

        public int Reviews { get; set; }
        public int Stock { get; set; }
        public string Status { get; set; } = string.Empty;
    }

    public class CreateBookRequest
    {
        public string Title { get; set; } = string.Empty;
        public string Author { get; set; } = string.Empty;
        public int? CategoryId { get; set; }
        public decimal Price { get; set; } = 0;
        public string? CoverImageUrl { get; set; }
        public string? Description { get; set; }
        public int StockQuantity { get; set; } = 1;
    }
}