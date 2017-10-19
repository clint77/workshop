using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace aspnetcore.Models
{
    public class Patient
    {
        public Patient()
        {
            Notes = new List<PatientNote>();
        }
        public string Id { get; set; }
        public string Type { get; set; }
        public PatientInformation Information { get; set; }
        public List<PatientNote> Notes { get; set; }
        public long Timestamp;
    }

    public class PatientInformation
    {
        [Required]
        public string FirstName { get; set; }
        [Required]
        public string LastName { get; set; }
        [Required]
        public string Gender { get; set; }
    }

    public class PatientNote
    {
        [Required]
        public string Doctor { get; set; }
        [Required]
        public string Message { get; set; }
        public long Timestamp { get; set; }
    }
}