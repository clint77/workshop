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
        cluster.authenticate(username, password);
        return cluster;
    }

    public @Bean
    Bucket bucket() {
        return cluster().openBucket(bucket);
    }

    @RequestMapping(value="/patients", method= RequestMethod.GET)
    public Object getPatients() {
        String statement = "SELECT META().id, `" + bucket().name() + "`.* FROM `" + bucket().name() + "` WHERE type = 'patient'";
        N1qlQueryResult result = bucket().query(N1qlQuery.simple(statement));
        return extractResultOrThrow(result);
        // The RxJava Approach
        /*return bucket().async().query(N1qlQuery.simple(statement))
            .flatMap(result -> result.errors().switchIfEmpty(result.rows().map(AsyncN1qlQueryRow::value)))
            .map(result -> result.toMap())
            .toList()
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/patient/{patientid}", method= RequestMethod.GET)
    public Object getPatientById(@PathVariable("patientid") String id) {
        return bucket().get(id).content().toMap();
        // The RxJava Approach
        /*return bucket().async().get(id)
            .map(result -> result.content().toMap())
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/patient/appointments/{patientid}", method= RequestMethod.GET)
    public Object getAppointmentsByPatientId(@PathVariable("patientid") String id) {
        String statement = "SELECT `" + bucket().name() + "`.* FROM `" + bucket().name() + "` WHERE type = 'appointment' AND patient = $id";
        JsonObject parameters = JsonObject.create().put("id", id);
        ParameterizedN1qlQuery query = ParameterizedN1qlQuery.parameterized(statement, parameters);
        N1qlQueryResult result = bucket().query(query);
        return extractResultOrThrow(result);
        // The RxJava Approach
        /*return bucket().async().query(query)
            .flatMap(result -> result.errors().switchIfEmpty(result.rows().map(AsyncN1qlQueryRow::value)))
            .map(result -> result.toMap())
            .toList()
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/patients/condition", method=RequestMethod.POST)
    public Object getPatientsByNotes(@RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
        if(jsonData.getString("search") == null || jsonData.getString("search") == "") {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `search` string is required").toString(), HttpStatus.BAD_REQUEST);
        }
        MatchQuery fts = SearchQuery.match(jsonData.getString("search"));
        if(jsonData.containsKey("fuzziness")) {
            fts.fuzziness(jsonData.getInt("fuzziness"));
        }
        SearchQuery query = new SearchQuery("medical-condition", fts);
        query.fields("information.firstname", "information.lastname", "notes.message");
        query.highlight(HighlightStyle.HTML, "notes.message");
        RawQueryExecutor rawQueryExecutor = new RawQueryExecutor(bucket().name(), username, password, bucket().core(), bucket().environment());
        return rawQueryExecutor.ftsToJsonObject(query).toMap();
    }

    @RequestMapping(value="/patient", method=RequestMethod.POST)
    public Object createPatient(@RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
        if(jsonData.getObject("information") == null) {
            return new ResponseEntity<String>(JsonObject.create().put("message", "An `information` object is required").toString(), HttpStatus.BAD_REQUEST);
        }
        jsonData.put("type", "patient");
        jsonData.put("timestamp", System.currentTimeMillis() / 1000L);
        if(jsonData.getObject("information") == null) {
            return new ResponseEntity<String>(JsonObject.create().put("message", "An `information` object is required").toString(), HttpStatus.BAD_REQUEST);
        }
        JsonDocument document = JsonDocument.create(UUID.randomUUID().toString(), jsonData);
        try {
            bucket().insert(document);
            return new ResponseEntity<String>(payload, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
        // The RxJava Approach
        /*return bucket().async().insert(document)
            .map(result -> payload)
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/patient/notes/{patientid}", method=RequestMethod.PUT)
    public Object createNoteByPatientId(@PathVariable("patientid") String id, @RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
        if(jsonData.getString("doctor") == null || jsonData.getString("doctor") == "") {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `doctor` string is required").toString(), HttpStatus.BAD_REQUEST);
        } else if(jsonData.getString("message") == null || jsonData.getString("message") == "") {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `message` string is required").toString(), HttpStatus.BAD_REQUEST);
        }
        jsonData.put("timestamp", System.currentTimeMillis() / 1000L);
        SubdocOptionsBuilder builder = new SubdocOptionsBuilder();
        builder.createParents(true);
        bucket().mutateIn(id).arrayAppend("notes", jsonData, builder).execute();
        return new ResponseEntity<String>(payload, HttpStatus.OK);
    }

    @RequestMapping(value="/doctors", method= RequestMethod.GET)
    public Object getDoctors() {
        String statement = "SELECT META().id, `" + bucket().name() + "`.* FROM `" + bucket().name() + "` WHERE type = 'doctor'";
        N1qlQueryResult result = bucket().query(N1qlQuery.simple(statement));
        return extractResultOrThrow(result);
        // The RxJava Approach
        /*return bucket().async().query(N1qlQuery.simple(statement))
            .flatMap(result -> result.errors().switchIfEmpty(result.rows().map(AsyncN1qlQueryRow::value)))
            .map(result -> result.toMap())
            .toList()
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/doctor/{doctorid}", method= RequestMethod.GET)
    public Object getDoctorById(@PathVariable("doctorid") String id) {
        return bucket().get(id).content().toMap();
        // The RxJava Approach
        /*return bucket().async().get(id)
            .map(result -> result.content().toMap())
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/doctor/patients/{doctorid}", method= RequestMethod.GET)
    public Object getPatientsByDoctorId(@PathVariable("doctorid") String id, @RequestParam(value = "serviced", required = false, defaultValue = "false") boolean serviced) {
        String statement = "SELECT patients.information, patients.timestamp, patients.type, META(patients).id FROM `" + bucket().name() + "` AS doctors JOIN `" + bucket().name() + "` AS patients ON KEYS doctors.patients WHERE doctors.type = 'doctor' AND META(doctors).id = $id";
        if(serviced == true) {
            statement = "SELECT patients.information, patients.timestamp, patients.type, META(patients).id FROM `" + bucket().name() + "` AS patients WHERE patients.type = 'patient' AND ANY note IN patients.notes SATISFIES note.doctor = $id END";
        }
        JsonObject parameters = JsonObject.create().put("id", id);
        ParameterizedN1qlQuery query = ParameterizedN1qlQuery.parameterized(statement, parameters);
        N1qlQueryResult result = bucket().query(query);
        return extractResultOrThrow(result);
        // The RxJava Approach
        /*return bucket().async().query(query)
            .flatMap(result -> result.errors().switchIfEmpty(result.rows().map(AsyncN1qlQueryRow::value)))
            .map(result -> result.toMap())
            .toList()
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/doctor/appointments/{doctorid}", method= RequestMethod.GET)
    public Object getAppointmentsByDoctorId(@PathVariable("doctorid") String id) {
        String statement = "SELECT `" + bucket().name() + "`.* FROM `" + bucket().name() + "` WHERE type = 'appointment' AND doctor = $id";
        JsonObject parameters = JsonObject.create().put("id", id);
        ParameterizedN1qlQuery query = ParameterizedN1qlQuery.parameterized(statement, parameters);
        N1qlQueryResult result = bucket().query(query);
        return extractResultOrThrow(result);
        // The RxJava Approach
        /*return bucket().async().query(query)
            .flatMap(result -> result.errors().switchIfEmpty(result.rows().map(AsyncN1qlQueryRow::value)))
            .map(result -> result.toMap())
            .toList()
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/doctor", method=RequestMethod.POST)
    public Object createDoctor(@RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
        jsonData.put("type", "doctor");
        jsonData.put("timestamp", System.currentTimeMillis() / 1000L);
        if(jsonData.getObject("information") == null) {
            return new ResponseEntity<String>(JsonObject.create().put("message", "An `information` object is required").toString(), HttpStatus.BAD_REQUEST);
        } else if(jsonData.getString("department") == null || jsonData.getString("department") == "") {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `department` string is required").toString(), HttpStatus.BAD_REQUEST);
        }
        JsonDocument document = JsonDocument.create(UUID.randomUUID().toString(), jsonData);
        try {
            bucket().insert(document);
            return new ResponseEntity<String>(payload, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
        // The RxJava Approach
        /*return bucket().async().insert(document)
            .map(result -> payload)
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/doctor/patient", method=RequestMethod.PUT)
    public Object addPatientForDoctor(@RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
        if(jsonData.getString("doctor") == null || jsonData.getString("doctor") == "") {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `doctor` string is required").toString(), HttpStatus.BAD_REQUEST);
        } else if(jsonData.getString("patient") == null || jsonData.getString("patient") == "") {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `patient` string is required").toString(), HttpStatus.BAD_REQUEST);
        }
        SubdocOptionsBuilder builder = new SubdocOptionsBuilder();
        builder.createParents(true);
        bucket().mutateIn(jsonData.getString("doctor")).arrayAddUnique("patients", jsonData.getString("patient"), builder).execute();
        return new ResponseEntity<String>(payload, HttpStatus.OK);
    }

    @RequestMapping(value="/appointments", method= RequestMethod.GET)
    public Object getAppointments() {
        String statement = "SELECT META().id, `" + bucket().name() + "`.* FROM `" + bucket().name() + "` WHERE type = 'appointment'";
        N1qlQueryResult result = bucket().query(N1qlQuery.simple(statement));
        return extractResultOrThrow(result);
        // The RxJava Approach
        /*return bucket().async().query(N1qlQuery.simple(statement))
            .flatMap(result -> result.errors().switchIfEmpty(result.rows().map(AsyncN1qlQueryRow::value)))
            .map(result -> result.toMap())
            .toList()
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/appointment", method=RequestMethod.POST)
    public Object createAppointment(@RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
        if(jsonData.getString("doctor") == null || jsonData.getString("doctor") == "") {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `doctor` string is required").toString(), HttpStatus.BAD_REQUEST);
        } else if(jsonData.getString("patient") == null || jsonData.getString("patient") == "") {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `patient` string is required").toString(), HttpStatus.BAD_REQUEST);
        } else if(jsonData.getLong("appointment") == null) {
            return new ResponseEntity<String>(JsonObject.create().put("message", "A `appointment` unix time is required").toString(), HttpStatus.BAD_REQUEST);
        }
        jsonData.put("type", "appointment");
        jsonData.put("timestamp", System.currentTimeMillis() / 1000L);
        JsonDocument document = JsonDocument.create(UUID.randomUUID().toString(), jsonData);
        try {
            bucket().insert(document);
            return new ResponseEntity<String>(payload, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
        // The RxJava Approach
        /*return bucket().async().insert(document)
            .map(result -> payload)
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
    }

    @RequestMapping(value="/appointment", method=RequestMethod.DELETE)
    public Object deleteAppointment(@RequestBody String payload) {
        JsonObject jsonData = JsonObject.fromJson(payload);
        if(jsonData.getString("id") == null || jsonData.getString("id") == "") {
            return new ResponseEntity<String>(JsonObject.create().put("message", "An `id` string is required").toString(), HttpStatus.BAD_REQUEST);
        }
        String statement = "DELETE FROM `" + bucket().name() + "` WHERE type = 'appointment' AND META().id = $id RETURNING *";
        JsonObject parameters = JsonObject.create().put("id", jsonData.getString("id"));
        ParameterizedN1qlQuery query = ParameterizedN1qlQuery.parameterized(statement, parameters);
        N1qlQueryResult result = bucket().query(query);
        return extractResultOrThrow(result);
        // The RxJava Approach
        /*return bucket().async().query(query)
            .flatMap(result -> result.errors().switchIfEmpty(result.rows().map(AsyncN1qlQueryRow::value)))
            .map(result -> result.toMap())
            .toList()
            .timeout(10, TimeUnit.SECONDS)
            .toBlocking()
            .single();*/
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