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
            /*
             * Step 4 - Querying for Multiple Documents with N1QL
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
            return CouchbaseError(result);
        }

        [ValidateModel]
        [HttpPost("appointment")]
        public async Task<IActionResult> Appointment([FromBody] DoctorAppointment appointment)
        {
            // type must be appointment, timestamp must be now, ignore/override any values posted in
            appointment.Type = "appointment";
            appointment.Timestamp = DateTimeOffset.Now.ToUnixTimeSeconds();

            /*
             * Step 2 - Creating NoSQL Documents within Couchbase
             *
             * Expected Results on Success...
             * {
             *     "appointment": 1604647429510,
             *     "doctor": "56fba9a4-7b28-4294-9d5c-f4841f4463ce",
             *     "patient": "0dce43e5-3cfa-441c-a833-9cdfa508e9a8",
             *     "type": "appointment",
             *     "timestamp": 1504647489510
             * }
             *
             * Expected Results on Error...
             * {
             *     "code": 500,
             *     "message": "Document key already exists"
             * }
             */
        }

        [HttpDelete("appointment")]
        public async Task<IActionResult> Appointment(string appointmentid)
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
    }
}
