using BookHubAPI.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BookHubAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class EventsController : ControllerBase
    {
        private readonly AppDbContext _context;

        public EventsController(AppDbContext context)
        {
            _context = context;
        }

        // ==================== SHARED ENDPOINTS ====================

        // GET: api/events (Admin & Mobile - Lấy tất cả sự kiện)
        [HttpGet]
        public async Task<ActionResult<IEnumerable<object>>> GetEvents([FromQuery] bool? mobileFormat = false)
        {
            var query = _context.Events.AsQueryable();

            if (mobileFormat == true)
            {
                // Format cho Mobile App
                var mobileEvents = await query
                    .Where(e => e.IsActive)
                    .Select(e => new
                    {
                        id = e.EventId,
                        title = e.Title,
                        description = e.Description ?? "",
                        startDate = e.StartDate != null ? e.StartDate.Value.ToString("dd/MM/yyyy") : "",
                        endDate = e.EndDate != null ? e.EndDate.Value.ToString("dd/MM/yyyy") : "",
                        imageUrl = GetFullImageUrl(e.ImageBannerUrl)
                    })
                    .ToListAsync();

                return Ok(mobileEvents);
            }
            else
            {
                // Format cho Admin Panel
                var events = await query
                    .Select(e => new
                    {
                        eventId = e.EventId,
                        title = e.Title,
                        description = e.Description,
                        startDate = e.StartDate,
                        endDate = e.EndDate,
                        imageBannerUrl = GetFullImageUrl(e.ImageBannerUrl),
                        isActive = e.IsActive
                    })
                    .ToListAsync();

                return Ok(events);
            }
        }

        // GET: api/events/5
        [HttpGet("{id}")]
        public async Task<ActionResult<Event>> GetEvent(int id)
        {
            var @event = await _context.Events.FindAsync(id);
            if (@event == null)
                return NotFound();

            return Ok(@event);
        }

        // ==================== MOBILE APP ENDPOINTS ====================

        // POST: api/events/register (Mobile - Đăng ký sự kiện)
        [HttpPost("register")]
        public async Task<ActionResult> RegisterEvent([FromBody] RegistrationRequest req)
        {
            // Kiểm tra đã đăng ký chưa
            var existing = await _context.EventRegistrations
                .FirstOrDefaultAsync(er => er.UserId == req.UserId && er.EventId == req.EventId);

            if (existing != null)
                return BadRequest(new { message = "Bạn đã đăng ký sự kiện này rồi!" });

            // Tạo đăng ký mới
            var registration = new EventRegistration
            {
                UserId = req.UserId,
                EventId = req.EventId,
                Status = "Đã đăng ký",
                RegisteredAt = DateTime.Now
            };

            _context.EventRegistrations.Add(registration);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Đăng ký thành công!" });
        }

        // GET: api/events/check-status?userId=1&eventId=1 (Mobile - Kiểm tra đã đăng ký chưa)
        [HttpGet("check-status")]
        public async Task<ActionResult> CheckRegistration([FromQuery] int userId, [FromQuery] int eventId)
        {
            var isRegistered = await _context.EventRegistrations
                .AnyAsync(er => er.UserId == userId && er.EventId == eventId);

            return Ok(new { isRegistered });
        }

        // GET: api/events/my-registrations?userId=1 (Mobile - Danh sách sự kiện đã đăng ký)
        [HttpGet("my-registrations")]
        public async Task<ActionResult> GetMyRegistrations([FromQuery] int userId)
        {
            var registrations = await _context.EventRegistrations
                .Include(er => er.Event)
                .Where(er => er.UserId == userId)
                .Select(er => new
                {
                    registrationId = er.RegistrationId,
                    eventId = er.EventId,
                    title = er.Event!.Title,
                    description = er.Event.Description ?? "",
                    startDate = er.Event.StartDate != null ? er.Event.StartDate.Value.ToString("dd/MM/yyyy") : "",
                    endDate = er.Event.EndDate != null ? er.Event.EndDate.Value.ToString("dd/MM/yyyy") : "",
                    imageUrl = GetFullImageUrl(er.Event.ImageBannerUrl),
                    status = er.Status,
                    registeredAt = er.RegisteredAt.ToString("dd/MM/yyyy")
                })
                .ToListAsync();

            return Ok(registrations);
        }

        // ==================== ADMIN ENDPOINTS ====================

        // POST: api/events (Admin - Tạo sự kiện mới)
        [HttpPost]
        public async Task<ActionResult> CreateEvent([FromBody] CreateEventRequest request)
        {
            var @event = new Event
            {
                Title = request.Title,
                Description = request.Description,
                StartDate = request.StartDate,
                EndDate = request.EndDate,
                ImageBannerUrl = request.ImageBannerUrl,
                IsActive = request.IsActive
            };

            _context.Events.Add(@event);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetEvent), new { id = @event.EventId }, @event);
        }

        // PUT: api/events/5 (Admin - Cập nhật sự kiện)
        [HttpPut("{id}")]
        public async Task<ActionResult> UpdateEvent(int id, [FromBody] CreateEventRequest request)
        {
            var @event = await _context.Events.FindAsync(id);
            if (@event == null)
                return NotFound();

            @event.Title = request.Title;
            @event.Description = request.Description;
            @event.StartDate = request.StartDate;
            @event.EndDate = request.EndDate;
            @event.ImageBannerUrl = request.ImageBannerUrl;
            @event.IsActive = request.IsActive;

            await _context.SaveChangesAsync();
            return NoContent();
        }

        // DELETE: api/events/5 (Admin - Xóa sự kiện)
        [HttpDelete("{id}")]
        public async Task<ActionResult> DeleteEvent(int id)
        {
            var @event = await _context.Events.FindAsync(id);
            if (@event == null)
                return NotFound();

            _context.Events.Remove(@event);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        // GET: api/events/5/registrations (Admin - Xem danh sách đăng ký của 1 sự kiện)
        [HttpGet("{id}/registrations")]
        public async Task<ActionResult> GetEventRegistrations(int id)
        {
            var registrations = await _context.EventRegistrations
                .Include(er => er.User)
                .Where(er => er.EventId == id)
                .Select(er => new
                {
                    registrationId = er.RegistrationId,
                    userId = er.UserId,
                    fullName = er.User!.FullName,
                    username = er.User.Username,
                    email = er.User.Email,
                    status = er.Status,
                    registeredAt = er.RegisteredAt.ToString("dd/MM/yyyy HH:mm")
                })
                .ToListAsync();

            return Ok(registrations);
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

    // ==================== DTOs ====================

    public class CreateEventRequest
    {
        public string Title { get; set; } = string.Empty;
        public string? Description { get; set; }
        public DateTime? StartDate { get; set; }
        public DateTime? EndDate { get; set; }
        public string? ImageBannerUrl { get; set; }
        public bool IsActive { get; set; } = true;
    }

    public class RegistrationRequest
    {
        public int UserId { get; set; }
        public int EventId { get; set; }
    }
}