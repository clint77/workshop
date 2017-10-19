using System;
using System.Threading.Tasks;
using aspnetcore.Models;
using Couchbase;
using Couchbase.Core;
using Couchbase.N1QL;
using Couchbase.Search;
using Couchbase.Search.Queries.Simple;
using Microsoft.AspNetCore.Mvc;

namespace aspnetcore.Controllers
{
    public class PatientController : BaseController
    {
        readonly IBucket _bucket;

        public PatientController(ICluster cluster)
        {
            _bucket = cluster.OpenBucket("default");
        }

        [HttpGet("patients")]
        public async Task<IActionResult> Patients()
        {
            var statement = $"SELECT META().id, `{_bucket.Name}`.* FROM `{_bucket.Name}` WHERE type='patient'";
            var query = QueryRequest.Create(statement);
            var result = await _bucket.QueryAsync<Patient>(query);
            if (result.Success)
                return Ok(result.Rows);
            return CouchbaseError(result);
        }

        [HttpGet("patient/{id}")]
        public async Task<IActionResult> Patient(string id)
        {
            var result = await _bucket.GetAsync<Patient>(id);
            if (result.Success)
                return Ok(result.Value);
            return CouchbaseError(result);
        }

        [HttpGet("patient/appointments/{patientid}")]
        public async Task<IActionResult> Appointments(string patientid)
        {
            var statement = $"SELECT `{_bucket.Name}`.* FROM `{_bucket.Name}` WHERE type = 'appointment' AND patient = $id";
            var query = QueryRequest.Create(statement);
            query.AddNamedParameter("id", patientid);
            var result = await _bucket.QueryAsync<DoctorAppointment>(query);
            if (result.Success)
                return Ok(result.Rows);
            return CouchbaseError(result);
        }

        [HttpPost("patients/condition")]
        public async Task<IActionResult> Condition(string search, int? fuzziness)
        {
            var match = new MatchQuery(search);
            if (fuzziness.HasValue)
                match.Fuzziness(fuzziness.Value);
            var query = new SearchQuery
            {
                Index = "medical-condition",
                Query = match,
                SearchParams = new SearchParams().Fields("information.firstname", "information.lastname", "notes.message")
            };
            query.Highlighting(HighLightStyle.Html, "notes.message");
            var result = await _bucket.QueryAsync(query);
            if (result.Success)
                return Ok(result.Hits);
            return CouchbaseError(result);
        }

        [ValidateModel]
        [HttpPost("patient")]
        public async Task<IActionResult> Patient([FromBody] Patient patient)
        {
            // type must be patient, timestamp must be now, ignore/override any values posted in
            patient.Type = "patient";
            patient.Timestamp = DateTimeOffset.Now.ToUnixTimeSeconds();

            var key = Guid.NewGuid().ToString();
            var result = await _bucket.InsertAsync(new Document<Patient>
            {
                Id = key,
                Content = patient
            });
            if (result.Success)
                return Ok(patient);
            return CouchbaseError(result);
        }

        [ValidateModel]
        [HttpPost("patient/notes/{patientid}")]
        public async Task<IActionResult> Notes(string patientid, [FromBody] PatientNote notes)
        {
            // timestamp must be now, ignore/override any values posted in
            notes.Timestamp = DateTimeOffset.Now.ToUnixTimeSeconds();

            IMutateInBuilder<dynamic> builder = _bucket.MutateIn<dynamic>(patientid)
                .ArrayAppend("notes", notes, false);
            var result = await builder.ExecuteAsync();

            if (result.Success)
                return Ok(notes);
            return CouchbaseError(result);
        }

    }
}