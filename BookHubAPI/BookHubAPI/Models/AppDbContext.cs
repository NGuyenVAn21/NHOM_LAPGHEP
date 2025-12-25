using Microsoft.EntityFrameworkCore;

namespace BookHubAPI.Models
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<User> Users { get; set; }
        public DbSet<Book> Books { get; set; }
        public DbSet<Category> Categories { get; set; }
        public DbSet<BorrowRecord> BorrowRecords { get; set; }
        public DbSet<Event> Events { get; set; }
        public DbSet<EventRegistration> EventRegistrations { get; set; } // ✅ THÊM DÒNG NÀY

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Book>()
                .HasOne(b => b.Category)
                .WithMany()
                .HasForeignKey(b => b.CategoryId);

            modelBuilder.Entity<BorrowRecord>()
                .HasOne(b => b.User)
                .WithMany()
                .HasForeignKey(b => b.UserId);

            modelBuilder.Entity<BorrowRecord>()
                .HasOne(b => b.Book)
                .WithMany()
                .HasForeignKey(b => b.BookId);

            // ✅ THÊM RELATIONSHIP CHO EventRegistration
            modelBuilder.Entity<EventRegistration>()
                .HasOne(er => er.User)
                .WithMany()
                .HasForeignKey(er => er.UserId);

            modelBuilder.Entity<EventRegistration>()
                .HasOne(er => er.Event)
                .WithMany()
                .HasForeignKey(er => er.EventId);
        }
    }
}