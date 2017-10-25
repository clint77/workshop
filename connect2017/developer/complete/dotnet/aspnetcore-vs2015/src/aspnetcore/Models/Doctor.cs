using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace aspnetcore.Models
{
    public class Doctor
    {
        public string Type { get; set; }
        public DoctorInformation Information { get; set; }
        [Required]
        public string Department { get; set; }
        public List<string> Patients { get; set; }
        public long Timestamp { get; set; }
    }

    public class DoctorInformation
    {
        [Required]
        public string LastName { get; set; }
        [Required]
        public string FirstName { get; set; }
        [Required]
        public string Gender { get; set; }
    }
}