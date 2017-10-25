using System;
using System.Threading.Tasks;
using aspnetcore.Models;
using Couchbase;
using Couchbase.Core;
using Couchbase.N1QL;
using Microsoft.AspNetCore.Mvc;

namespace aspnetcore.Controllers
{
    public class AppointmentController : BaseController
    {
        readonly IBucket _bucket;

        public AppointmentController(ICluster cluster)
        {
            _bucket = cluster.OpenBucket("default");
        }

        [HttpGet("appointments")]
        public async Task<IActionResult> Appointments()
        {
            var statement = $"SELECT META().id, `{_bucket.Name}`.* FROM `{_bucket.Name}` WHERE type = 'appointment'";
            var query = QueryRequest.Create(statement);
            var result = await _bucket.QueryAsync<DoctorAppointment>(query);
            if (result.Success)
                return Ok(result.Rows);
            return CouchbaseError(result);
        }

        [ValidateModel]
        [HttpPost("appointment")]
        public async Task<IActionResult> Appointment([FromBody] DoctorAppointment appointment)
        {
            // type must be appointment, timestamp must be now, ignore/override any values posted in
            appointment.Type = "appointment";
            appointment.Timestamp = DateTimeOffset.Now.ToUnixTimeSeconds();

            var key = Guid.NewGuid().ToString();
            var result = await _bucket.InsertAsync(new Document<DoctorAppointment>
            {
                Id = key,
                Content = appointment
            });
            if (result.Success)
                return Ok(appointment);
            return CouchbaseError(result);
        }

        [HttpDelete("appointment")]
        public async Task<IActionResult> Appointment(string appointmentid)
        {
            var n1ql = $"DELETE FROM `{_bucket.Name}` WHERE type = 'appointment' AND META().id = $id RETURNING *;";
            var query = QueryRequest.Create(n1ql);
            query.AddNamedParameter("id", appointmentid);
            var result = await _bucket.QueryAsync<dynamic>(query);
            if (result.Success)
                return Ok(result.Rows);
            return CouchbaseError(result);
        }
    }
}
