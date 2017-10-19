using System.ComponentModel.DataAnnotations;

namespace aspnetcore.Models
{
    public class PatientAssign
    {
        [Required]
        public string Doctor { get; set; }
        [Required]
        public string Patient { get; set; }
    }
}