using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BookHubAPI.Models
{
    [Table("Users")]
    public class User
    {
        [Key]
        [Column("user_id")]
        public int UserId { get; set; }

        [Required, MaxLength(50)]
        [Column("username")]
        public string Username { get; set; } = string.Empty;

        [Required, EmailAddress, MaxLength(100)]
        [Column("email")]
        public string Email { get; set; } = string.Empty;

        [Required, MaxLength(255)]
        [Column("password_hash")]
        public string PasswordHash { get; set; } = string.Empty;

        [Required, MaxLength(100)]
        [Column("full_name")]
        public string FullName { get; set; } = string.Empty;

        [MaxLength(15)]
        [Column("phone_number")]
        public string? PhoneNumber { get; set; }

        [Column("avatar_url")]
        public string? AvatarUrl { get; set; }

        [Column("created_at")]
        public DateTime CreatedAt { get; set; } = DateTime.Now;

        [Column("is_active")]
        public bool IsActive { get; set; } = true;
    }
}