using Microsoft.AspNetCore.Mvc;
using System.Data.SqlClient;
using Microsoft.Extensions.Configuration;
using System.Collections.Generic;
using System;

namespace BookHubAPI.Controllers
{
    [Route("api/events")]
    [ApiController]
    public class EventsController : ControllerBase
    {
        private readonly IConfiguration _configuration;

        public EventsController(IConfiguration configuration)
        {
            _configuration = configuration;
        }

        [HttpGet]
        public IActionResult GetAllEvents()
        {
            var events = new List<object>();
            try
            {
                string connectionString = _configuration.GetConnectionString("DefaultConnection");
                using (SqlConnection conn = new SqlConnection(connectionString))
                {
                    conn.Open();
                    string sql = "SELECT event_id, title, description, start_date, end_date, image_banner_url FROM Events WHERE is_active = 1";

                    using (SqlCommand cmd = new SqlCommand(sql, conn))
                    {
                        using (SqlDataReader reader = cmd.ExecuteReader())
                        {
                            while (reader.Read())
                            {
                                events.Add(new
                                {
                                    id = Convert.ToInt32(reader["event_id"]),
                                    title = reader["title"].ToString(),
                                    description = reader["description"].ToString(),
                                    startDate = Convert.ToDateTime(reader["start_date"]).ToString("dd/MM/yyyy"),
                                    endDate = Convert.ToDateTime(reader["end_date"]).ToString("dd/MM/yyyy"),
                                    imageUrl = reader["image_banner_url"] != DBNull.Value ? reader["image_banner_url"].ToString() : ""
                                });
                            }
                        }
                    }
                }
                return Ok(events);
            }
            catch (Exception ex)
            {
                return StatusCode(500, "Lỗi Server: " + ex.Message);
            }
        }
    }
}