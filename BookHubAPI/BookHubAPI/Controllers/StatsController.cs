using BookHubAPI.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BookHubAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class StatsController : ControllerBase
    {
        private readonly AppDbContext _context;

        public StatsController(AppDbContext context)
        {
            _context = context;
        }

        // GET: api/stats/active-readers (Mobile - Top 5 độc giả tích cực)
        [HttpGet("active-readers")]
        public async Task<ActionResult> GetActiveReaders()
        {
            var users = await _context.Users
                .Select(u => new
                {
                    User = u,
                    TotalBorrow = _context.BorrowRecords.Count(br => br.UserId == u.UserId)
                })
                .OrderByDescending(x => x.TotalBorrow)
                .Take(5)
                .Select(x => new
                {
                    id = x.User.UserId,
                    name = x.User.FullName,
                    avatar = GetFullImageUrl(x.User.AvatarUrl),
                    borrowCount = x.TotalBorrow
                })
                .ToListAsync();

            return Ok(users);
        }

        // GET: api/stats/user-summary?userId=1 (Mobile - Thống kê của 1 user)
        [HttpGet("user-summary")]
        public async Task<ActionResult> GetUserSummary([FromQuery] int userId)
        {
            // 1. Đếm sách đang mượn
            var borrowCount = await _context.BorrowRecords
                .CountAsync(br => br.UserId == userId && br.Status == "Borrowing");

            // 2. Đếm sách sắp đến hạn (còn 3 ngày)
            var dueSoonCount = await _context.BorrowRecords
                .CountAsync(br => br.UserId == userId &&
                                 br.Status == "Borrowing" &&
                                 br.DueDate <= DateTime.Now.AddDays(3));

            return Ok(new
            {
                borrowing = borrowCount,
                dueSoon = dueSoonCount
            });
        }

        // ==================== THỐNG KÊ CHO ADMIN ====================

        // GET: api/stats/dashboard (Admin - Thống kê tổng quan)
        [HttpGet("dashboard")]
        public async Task<ActionResult> GetDashboardStats()
        {
            var totalBooks = await _context.Books.CountAsync();
            var totalUsers = await _context.Users.CountAsync();
            var borrowingCount = await _context.BorrowRecords.CountAsync(b => b.Status == "Borrowing");
            var overdueCount = await _context.BorrowRecords.CountAsync(b => b.Status == "Overdue");
            var activeEvents = await _context.Events.CountAsync(e => e.IsActive);

            // Top 5 sách được mượn nhiều nhất
            var topBooks = await _context.BorrowRecords
                .GroupBy(br => br.BookId)
                .Select(g => new
                {
                    BookId = g.Key,
                    BorrowCount = g.Count()
                })
                .OrderByDescending(x => x.BorrowCount)
                .Take(5)
                .Join(_context.Books,
                    x => x.BookId,
                    b => b.BookId,
                    (x, b) => new
                    {
                        bookId = b.BookId,
                        title = b.Title,
                        author = b.Author,
                        borrowCount = x.BorrowCount
                    })
                .ToListAsync();

            return Ok(new
            {
                totalBooks,
                totalUsers,
                borrowingCount,
                overdueCount,
                activeEvents,
                topBooks
            });
        }

        // GET: api/stats/book-status (Admin - Thống kê trạng thái sách)
        [HttpGet("book-status")]
        public async Task<ActionResult> GetBookStatus()
        {
            var available = await _context.Books.CountAsync(b => b.StockQuantity > 0);
            var outOfStock = await _context.Books.CountAsync(b => b.StockQuantity == 0);

            return Ok(new
            {
                available,
                outOfStock
            });
        }

        // GET: api/stats/monthly-borrows (Admin - Thống kê mượn theo tháng)
        [HttpGet("monthly-borrows")]
        public async Task<ActionResult> GetMonthlyBorrows([FromQuery] int year = 0)
        {
            if (year == 0) year = DateTime.Now.Year;

            var monthlyData = await _context.BorrowRecords
                .Where(br => br.BorrowDate.Year == year)
                .GroupBy(br => br.BorrowDate.Month)
                .Select(g => new
                {
                    month = g.Key,
                    count = g.Count()
                })
                .OrderBy(x => x.month)
                .ToListAsync();

            // Tạo data cho 12 tháng (fill 0 nếu không có)
            var result = Enumerable.Range(1, 12).Select(month => new
            {
                month = $"Tháng {month}",
                count = monthlyData.FirstOrDefault(x => x.month == month)?.count ?? 0
            });

            return Ok(result);
        }

        // GET: api/stats/category-distribution (Admin - Phân bố sách theo danh mục)
        [HttpGet("category-distribution")]
        public async Task<ActionResult> GetCategoryDistribution()
        {
            var distribution = await _context.Books
                .Include(b => b.Category)
                .GroupBy(b => b.Category!.CategoryName)
                .Select(g => new
                {
                    category = g.Key,
                    count = g.Count()
                })
                .OrderByDescending(x => x.count)
                .ToListAsync();

            return Ok(distribution);
        }

        // ==================== HELPER METHODS ====================

        private string GetFullImageUrl(string? fileName)
        {
            if (string.IsNullOrEmpty(fileName))
                return "";

            if (fileName.StartsWith("http"))
                return fileName;

            return $"{Request.Scheme}://{Request.Host}/images/{fileName}";
        }
    }
}