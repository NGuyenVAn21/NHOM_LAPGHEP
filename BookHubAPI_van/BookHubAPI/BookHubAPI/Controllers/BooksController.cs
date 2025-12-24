// Controllers/BooksController.cs
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
                    Category = b.Category?.CategoryName ?? "Khác",
                    Price = b.Price,
                    CoverImageUrl = b.CoverImageUrl,
                    Rating = b.AverageRating,
                    Reviews = b.ReviewCount,
                    Status = b.CurrentStatus,
                    Stock = b.StockQuantity
                })
                .ToListAsync();
            return Ok(books);
        }

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
                    Category = b.Category?.CategoryName ?? "Khác",
                    Price = b.Price,
                    CoverImageUrl = b.CoverImageUrl,
                    Rating = b.AverageRating,
                    Reviews = b.ReviewCount,
                    Status = b.CurrentStatus,
                    Stock = b.StockQuantity
                })
                .ToListAsync();
            return Ok(books);
        }

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
                Rating = book.AverageRating,
                Reviews = book.ReviewCount,
                Stock = book.StockQuantity,
                Status = book.CurrentStatus
            });
        }

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
    }

    // DTOs
    public class BookDto
    {
        public int Id { get; set; }
        public string Title { get; set; } = string.Empty;
        public string Author { get; set; } = string.Empty;
        public string Category { get; set; } = string.Empty;
        public decimal Price { get; set; }
        public string? CoverImageUrl { get; set; }
        public float Rating { get; set; }
        public int Reviews { get; set; }
        public string Status { get; set; } = string.Empty;
        public int Stock { get; set; }
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
        public float Rating { get; set; }
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