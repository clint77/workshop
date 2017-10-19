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
            /*
             * Step 4 - Querying for Multiple Documents with N1QL
             *
             * Expected Results on Success...
             * [
             *     {
             *         "id": "0dce43e5-3cfa-441c-a833-9cdfa508e9a8",
             *         "information": {
             *             "firstname": "Nic",
             *             "lastname": "Raboy",
             *             "gender": "Male"
             *         },
             *         "type": "patient",
             *         "timestamp": 1504640069494,
             *         "notes": [
             *             {
             *                 "doctor": "56fba9a4-7b28-4294-9d5c-f4841f4463ce",
             *                 "message": "The patient is way too smart",
             *                 "timestamp": 1504640200089
             *             }
             *         ]
             *     }
             * ]
             *
             * Expected Results on Error...
             * {
             *     "code": 500,
             *     "message": "Malformed query"
             * }
             */
        }

        [HttpGet("patient/{id}")]
        public async Task<IActionResult> Patient(string id)
        {
            /*
             * Step 3 - Getting Data by Document Id
             *
             * Expected Results on Success...
             * {
             *     "information": {
             *         "firstname": "Nic",
             *         "lastname": "Raboy",
             *         "gender": "Male"
             *     },
             *     "type": "patient",
             *     "timestamp": 1504640069494,
             *     "notes": [
             *         {
             *             "doctor": "56fba9a4-7b28-4294-9d5c-f4841f4463ce",
             *             "message": "The patient is way too smart",
             *             "timestamp": 1504640200089
             *         }
             *     ]
             * }
             *
             * Expected Results on Error...
             * {
             *     "code": 500,
             *     "message": "No document key exists"
             * }
             */
        }

        [HttpGet("patient/appointments/{patientid}")]
        public async Task<IActionResult> Appointments(string patientid)
        {
            /*
             * Step 5 - Using Parameterized Queries with N1QL
             *
             * Expected Results on Success...
             * [
             *     {
             *         "id": "18fba9a4-7b28-4294-9d5c-f4841f4463ce",
             *         "appointment": 1604647429510,
             *         "doctor": "56fba9a4-7b28-4294-9d5c-f4841f4463ce",
             *         "patient": "0dce43e5-3cfa-441c-a833-9cdfa508e9a8",
             *         "type": "appointment",
             *         "timestamp": 1504647489510
             *     }
             * ]
             *
             * Expected Results on Error...
             * {
             *     "code": 500,
             *     "message": "Malformed query"
             * }
             */
        }

        [HttpPost("patients/condition")]
        public async Task<IActionResult> Condition(string search, int? fuzziness)
        {
            /*
             * Step 8 - Searching within a Document using Full Text Search
             *
             * Expected Results on Success...
             * [
             *     {
             *         "index": "medical-condition_22a0d1298e333302_acbbef99",
             *         "id": "0dce43e5-3cfa-441c-a833-9cdfa508e9a8",
             *         "score": 0.7571984292622859,
             *         "locations": {
             *             "notes.message": {
             *                 "smart": [
             *                     {
             *                         "pos": 6,
             *                         "start": 23,
             *                         "end": 28,
             *                         "array_positions": [
             *                             0
             *                         ]
             *                     }
             *                 ]
             *             }
             *         },
             *         "fragments": {
             *             "notes.message": [
             *                 "The patient is way too <mark>smart</mark>"
             *             ]
             *         },
             *         "sort": [
             *             "_score"
             *         ],
             *         "fields": {
             *             "information.firstname": "Nic",
             *             "information.lastname": "Raboy",
             *             "notes.message": "The patient is way too smart"
             *         }
             *     }
             * ]
             *
             * Expected Results on Error...
             * {
             *     "code": 500,
             *     "message": "Malformed query"
             * }
             */
        }

        [ValidateModel]
        [HttpPost("patient")]
        public async Task<IActionResult> Patient([FromBody] Patient patient)
        {
            // type must be patient, timestamp must be now, ignore/override any values posted in
            patient.Type = "patient";
            patient.Timestamp = DateTimeOffset.Now.ToUnixTimeSeconds();

            /*
             * Step 2 - Creating NoSQL Documents within Couchbase
             *
             * Expected Results on Success...
             * {
             *     "information": {
             *         "firstname": "Nic",
             *         "lastname": "Raboy",
             *         "gender": "Male"
             *     },
             *     "type": "patient",
             *     "timestamp": 1504646525541
             * }
             *
             * Expected Results on Error...
             * {
             *     "code": 500,
             *     "message": "Document key already exists"
             * }
             */
        }

        [ValidateModel]
        [HttpPost("patient/notes/{patientid}")]
        public async Task<IActionResult> Notes(string patientid, [FromBody] PatientNote notes)
        {
            // timestamp must be now, ignore/override any values posted in
            notes.Timestamp = DateTimeOffset.Now.ToUnixTimeSeconds();

            /*
             * Step 7 - Mutating Part of a JSON Document
             *
             * Expected Results on Success...
             * {
             *     "doctor": "56fba9a4-7b28-4294-9d5c-f4841f4463ce",
             *     "message": "The patient is way too smart",
             *     "timestamp": 1504646525542
             * }
             *
             * Expected Results on Error...
             * {
             *     "code": 500,
             *     "message": "Document key does not exist"
             * }
             */
        }

    }
}