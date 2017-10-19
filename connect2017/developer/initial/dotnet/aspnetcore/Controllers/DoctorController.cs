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
            /*
             * Step 4 - Querying for Multiple Documents with N1QL
             *
             * Expected Results on Success...
             * [
             *     {
             *         "id": "56fba9a4-7b28-4294-9d5c-f4841f4463ce",
             *         "information": {
             *             "firstname": "Victor",
             *             "lastname": "Frankenstein",
             *             "gender": "Male"
             *         },
             *         "type": "doctor",
             *         "timestamp": 1504640069494,
             *         "department": "Emergency Room",
             *         "patients": [
             *             "0dce43e5-3cfa-441c-a833-9cdfa508e9a8"
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

        [HttpGet("doctor/{id}")]
        public async Task<IActionResult> Doctor(string id)
        {
            /*
             * Step 3 - Getting Data by Document Id
             *
             * Expected Results on Success...
             * {
             *     "information": {
             *         "firstname": "Victor",
             *         "lastname": "Frankenstein",
             *         "gender": "Male"
             *     },
             *     "type": "doctor",
             *     "timestamp": 1504640069494,
             *     "department": "Emergency Room",
             *     "patients": [
             *         "0dce43e5-3cfa-441c-a833-9cdfa508e9a8"
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

        [HttpGet("doctor/patients/{doctorid}")]
        public async Task<IActionResult> DoctorsPatients(string doctorid, bool serviced = false)
        {
            /*
             * Step 6 - Executing Complex Queries with N1QL
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
             *         "timestamp": 1504640069494,
             *         "type": "patient"
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

        [HttpGet("doctor/appointments/{doctorid}")]
        public async Task<IActionResult> DoctorsAppointments(string doctorid)
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

        [ValidateModel]
        [HttpPost("doctor")]
        public async Task<IActionResult> Doctor([FromBody] Doctor doctor)
        {
            // type must be doctor, timestamp must be now, ignore/override any values posted in
            doctor.Type = "doctor";
            doctor.Timestamp = DateTimeOffset.Now.ToUnixTimeSeconds();

            /*
             * Step 2 - Creating NoSQL Documents within Couchbase
             *
             * Expected Results on Success...
             * {
             *     "information": {
             *         "firstname": "Victor",
             *         "lastname": "Frankenstein",
             *         "gender": "Male"
             *     },
             *     "type": "doctor",
             *     "department": "Emergency Room",
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
        [HttpPut("doctor/patient")]
        public async Task<IActionResult> DoctorPatient([FromBody] PatientAssign assign)
        {
            /*
             * Step 7 - Mutating Part of a JSON Document
             *
             * Expected Results on Success...
             * {
             *     "doctor": "56fba9a4-7b28-4294-9d5c-f4841f4463ce",
             *     "patient": "f059fbb9-d2ea-48e1-8129-bc084c8b423f"
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