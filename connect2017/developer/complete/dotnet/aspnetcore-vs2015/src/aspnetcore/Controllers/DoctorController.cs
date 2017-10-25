using System;
using System.Threading.Tasks;
using aspnetcore.Models;
using Couchbase;
using Couchbase.Core;
using Couchbase.N1QL;
using Microsoft.AspNetCore.Mvc;

namespace aspnetcore.Controllers
{
    public class DoctorController : BaseController
    {
        readonly IBucket _bucket;

        public DoctorController(ICluster cluster)
        {
            _bucket = cluster.OpenBucket("default");
        }

        [HttpGet("doctors")]
        public async Task<IActionResult> Doctors()
        {
            var statement = $"SELECT META().id, `{_bucket.Name}`.* FROM `{_bucket.Name}` WHERE type = 'doctor'";
            var query = QueryRequest.Create(statement);
            var result = await _bucket.QueryAsync<Doctor>(query);
            if (result.Success)
                return Ok(result.Rows);
            return CouchbaseError(result);
        }

        [HttpGet("doctor/{id}")]
        public async Task<IActionResult> Doctor(string id)
        {
            var result = await _bucket.GetAsync<Doctor>(id);
            if (result.Success)
                return Ok(result.Value);
            return CouchbaseError(result);
        }

        [HttpGet("doctor/patients/{doctorid}")]
        public async Task<IActionResult> DoctorsPatients(string doctorid, bool serviced = false)
        {
            string statement;
            if (serviced)
                statement = $@"SELECT patients.information, patients.timestamp, patients.type, META(patients).id
                                FROM `{_bucket.Name}` AS patients
                                WHERE patients.type = 'patient'
                                AND ANY note IN patients.notes SATISFIES note.doctor = $id END";
            else
                statement = $@"SELECT patients.information, patients.timestamp, patients.type, META(patients).id
                                FROM `{_bucket.Name}` AS doctors
                                JOIN `{_bucket.Name}` AS patients ON KEYS doctors.patients
                                WHERE doctors.type = 'doctor'
                                AND META(doctors).id = $id";

            var query = QueryRequest.Create(statement);
            query.AddNamedParameter("id", doctorid);
            var result = await _bucket.QueryAsync<Patient>(query);
            if (result.Success)
                return Ok(result.Rows);
            return CouchbaseError(result);
        }

        [HttpGet("doctor/appointments/{doctorid}")]
        public async Task<IActionResult> DoctorsAppointments(string doctorid)
        {
            var statement = $"SELECT `{_bucket.Name}`.* FROM `{_bucket.Name}` WHERE type = 'appointment' AND doctor = $id";
            var query = QueryRequest.Create(statement);
            query.AddNamedParameter("id", doctorid);
            var result = await _bucket.QueryAsync<DoctorAppointment>(query);
            if (result.Success)
                return Ok(result.Rows);
            return CouchbaseError(result);
        }

        [ValidateModel]
        [HttpPost("doctor")]
        public async Task<IActionResult> Doctor([FromBody] Doctor doctor)
        {
            // type must be doctor, timestamp must be now, ignore/override any values posted in
            doctor.Type = "doctor";
            doctor.Timestamp = DateTimeOffset.Now.ToUnixTimeSeconds();

            var key = Guid.NewGuid().ToString();
            var result = await _bucket.InsertAsync(new Document<Doctor>
            {
                Id = key,
                Content = doctor
            });
            if (result.Success)
                return Ok(doctor);
            return CouchbaseError(result);
        }

        [ValidateModel]
        [HttpPut("doctor/patient")]
        public async Task<IActionResult> DoctorPatient([FromBody] PatientAssign assign)
        {
            IMutateInBuilder<dynamic> builder = _bucket.MutateIn<dynamic>(assign.Doctor);
            builder.ArrayAddUnique("patients", assign.Patient, true);
            var result = await builder.ExecuteAsync();
            if (result.Success)
                return Ok(assign);
            return CouchbaseError(result);
        }
    }
}