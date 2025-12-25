using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BookHubAPI.Models
{
    [Table("EventRegistrations")]
    public class EventRegistration
    {
        [Key]
        public int RegistrationId { get; set; }

        [Required]
        public int UserId { get; set; }

        [Required]
        public int EventId { get; set; }

        [Required]
        [MaxLength(50)]
        public string Status { get; set; } = "Đã đăng ký";

        [Required]
        public DateTime RegisteredAt { get; set; } = DateTime.Now;

        // Navigation Properties
        public User? User { get; set; }
        public Event? Event { get; set; }
    }
}