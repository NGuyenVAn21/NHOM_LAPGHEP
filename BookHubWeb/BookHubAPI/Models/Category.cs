using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BookHubAPI.Models
{
    [Table("Categories")]
    public class Category
    {
        [Key]
        [Column("category_id")]
        public int CategoryId { get; set; }

        [Required, MaxLength(100)]
        [Column("category_name")]
        public string CategoryName { get; set; } = string.Empty;

        [Column("description")]
        public string? Description { get; set; }
    }
}