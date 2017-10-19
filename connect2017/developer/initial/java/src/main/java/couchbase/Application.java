package couchbase;

import com.couchbase.client.core.message.kv.subdoc.multi.Lookup;
import com.couchbase.client.java.*;
import com.couchbase.client.java.auth.Authenticator;
import com.couchbase.client.java.env.*;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.query.*;
import com.couchbase.client.java.query.consistency.ScanConsistency;
import com.couchbase.client.java.search.HighlightStyle;
import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.queries.MatchQuery;
import com.couchbase.client.java.search.queries.QueryStringQuery;
import com.couchbase.client.java.search.result.SearchQueryResult;
import com.couchbase.client.java.subdoc.DocumentFragment;
import com.couchbase.client.java.subdoc.SubdocOptionsBuilder;
import com.couchbase.client.java.util.rawQuerying.RawQueryExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.bind.annotation.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
@RequestMapping("/")
public class Application implements Filter {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}

    @Value("${hostname}")
    private String hostname;

    @Value("${bucket}")
    private String bucket;

    @Value("${rbac.username}")
    private String username;

    @Value("${rbac.password}")
    private String password;

    public @Bean
    Cluster cluster() {
        CouchbaseCluster cluster = CouchbaseCluster.create(hostname);

        /*
         * Step 1 - Authenticating with a Role Based Account
         * **************** PUT CODE HERE ******************
         */

        return cluster;
    }

    public @Bean
    Bucket bucket() {
        return cluster().openBucket(bucket);
    }

    @RequestMapping(value="/patients", method= RequestMethod.GET)
    public Object getPatients() {
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

    @RequestMapping(value="/patient/{patientid}", method= RequestMethod.GET)
    public Object getPatientById(@PathVariable("patientid") String id) {
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

    @RequestMapping(value="/patient/appointments/{patientid}", method= RequestMethod.GET)
    public Object getAppointmentsByPatientId(@PathVariable("patientid") String id) {
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

    @RequestMapping(value="/patients/condition", method=RequestMethod.POST)
    public Object getPatientsByNotes(@RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
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
        RawQueryExecutor rawQueryExecutor = new RawQueryExecutor(bucket().name(), username, password, bucket().core(), bucket().environment());
        return rawQueryExecutor.ftsToJsonObject(query).toMap();
    }

    @RequestMapping(value="/patient", method=RequestMethod.POST)
    public Object createPatient(@RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
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

    @RequestMapping(value="/patient/notes/{patientid}", method=RequestMethod.PUT)
    public Object createNoteByPatientId(@PathVariable("patientid") String id, @RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
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

    @RequestMapping(value="/doctors", method= RequestMethod.GET)
    public Object getDoctors() {
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

    @RequestMapping(value="/doctor/{doctorid}", method= RequestMethod.GET)
    public Object getDoctorById(@PathVariable("doctorid") String id) {
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

    @RequestMapping(value="/doctor/patients/{doctorid}", method= RequestMethod.GET)
    public Object getPatientsByDoctorId(@PathVariable("doctorid") String id, @RequestParam(value = "serviced", required = false, defaultValue = "false") boolean serviced) {
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

    @RequestMapping(value="/doctor/appointments/{doctorid}", method= RequestMethod.GET)
    public Object getAppointmentsByDoctorId(@PathVariable("doctorid") String id) {
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

    @RequestMapping(value="/doctor", method=RequestMethod.POST)
    public Object createDoctor(@RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
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

    @RequestMapping(value="/doctor/patient", method=RequestMethod.PUT)
    public Object addPatientForDoctor(@RequestBody String payload) {
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

    @RequestMapping(value="/appointments", method= RequestMethod.GET)
    public Object getAppointments() {
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
    }

    @RequestMapping(value="/appointment", method=RequestMethod.POST)
    public Object createAppointment(@RequestBody String payload) {
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

    @RequestMapping(value="/appointment", method=RequestMethod.DELETE)
    public Object deleteAppointment(@RequestBody String payload) {
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

    private static List<Map<String, Object>> extractResultOrThrow(N1qlQueryResult result) {
        if (!result.finalSuccess()) {
            throw new DataRetrievalFailureException("Query Error: " + result.errors());
        }
        List<Map<String, Object>> content = new ArrayList<Map<String, Object>>();
        for (N1qlQueryRow row : result) {
            content.add(row.value().toMap());
        }
        return content;
    }

}