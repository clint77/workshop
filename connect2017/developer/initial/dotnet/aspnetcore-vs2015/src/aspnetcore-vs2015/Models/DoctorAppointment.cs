using System.ComponentModel.DataAnnotations;

namespace aspnetcore.Models
{
    public class DoctorAppointment
    {
        public string Type { get; set; }
        [Required]
        public string Doctor { get; set; }
        [Required]
        public string Patient { get; set; }
        [Required]
        public int Appointment { get; set; }
        public long Timestamp { get; set; }
    }
}