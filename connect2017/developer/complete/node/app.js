const Couchbase = require("couchbase");
const Hapi = require("hapi");
const Joi = require("Joi");
const UUID = require("uuid");

const server = new Hapi.Server();
var N1qlQuery = Couchbase.N1qlQuery;

const cluster = new Couchbase.Cluster("couchbase://localhost");
cluster.authenticate("demo", "123456");
const bucket = cluster.openBucket("default");

bucket.on("error", error => {
    console.dir(error);
    process.exit(1);
});

server.connection({ "host": "localhost", "port": 3000 });

server.route({
    method: "GET",
    path: "/patients",
    handler: (request, response) => {
        var statement = "SELECT META().id, `" + bucket._name + "`.* FROM `" + bucket._name + "` WHERE type = 'patient'";
        var query = N1qlQuery.fromString(statement);
        bucket.query(query, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(result);
        });
    }
});

server.route({
    method: "GET",
    path: "/patient/{id}",
    config: {
        validate: {
            params: {
                id: Joi.string().required()
            }
        }
    },
    handler: (request, response) => {
        bucket.get(request.params.id, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(result.value);
        });
    }
});

server.route({
    method: "GET",
    path: "/patient/appointments/{patientid}",
    config: {
        validate: {
            params: {
                patientid: Joi.string().required()
            }
        }
    },
    handler: (request, response) => {
        var statement = "SELECT `" + bucket._name + "`.* FROM `" + bucket._name + "` WHERE type = 'appointment' AND patient = $id";
        var query = N1qlQuery.fromString(statement);
        bucket.query(query, { "id": request.params.patientid }, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(result);
        });
    }
});

server.route({
    method: "POST",
    path: "/patients/condition",
    config: {
        validate: {
            payload: {
                search: Joi.string().required(),
                fuzziness: Joi.number().integer().optional()
            }
        }
    },
    handler: (request, response) => {
        var SearchQuery = Couchbase.SearchQuery;
        if(!request.payload.fuzziness) {
            var query = SearchQuery.new("medical-condition", SearchQuery.match(request.payload.search));
        } else {
            var query = SearchQuery.new("medical-condition", SearchQuery.match(request.payload.search).fuzziness(request.payload.fuzziness));
        }
        query.fields(["information.firstname", "information.lastname", "notes.message"]);
        query.highlight(SearchQuery.HighlightStyle.HTML, "notes.message");
        bucket.query(query, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(result);
        });
    }
});

server.route({
    method: "POST",
    path: "/patient",
    config: {
        validate: {
            payload: {
                information: {
                    firstname: Joi.string().required(),
                    lastname: Joi.string().required(),
                    gender: Joi.string().required()
                },
                type: Joi.any().forbidden().default("patient"),
                timestamp: Joi.any().forbidden().default((new Date()).getTime())
            }
        }
    },
    handler: (request, response) => {
        bucket.insert(UUID.v4(), request.payload, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(request.payload);
        });
    }
});

server.route({
    method: "PUT",
    path: "/patient/notes/{patientid}",
    config: {
        validate: {
            params: {
                patientid: Joi.string().required()
            },
            payload: {
                doctor: Joi.string().required(),
                message: Joi.string().required(),
                timestamp: Joi.any().forbidden().default((new Date()).getTime())
            }
        }
    },
    handler: (request, response) => {
        bucket.mutateIn(request.params.patientid, 0, 0).arrayAppend("notes", request.payload, true).execute((error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(request.payload);
        });
    }
});

server.route({
    method: "GET",
    path: "/doctors",
    handler: (request, response) => {
        var statement = "SELECT META().id, `" + bucket._name + "`.* FROM `" + bucket._name + "` WHERE type = 'doctor'";
        var query = N1qlQuery.fromString(statement);
        bucket.query(query, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(result);
        });
    }
});

server.route({
    method: "GET",
    path: "/doctor/{id}",
    config: {
        validate: {
            params: {
                id: Joi.string().required()
            }
        }
    },
    handler: (request, response) => {
        bucket.get(request.params.id, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(result.value);
        });
    }
});

server.route({
    method: "GET",
    path: "/doctor/patients/{doctorid}",
    config: {
        validate: {
            params: {
                doctorid: Joi.string().required()
            },
            query: {
                serviced: Joi.boolean().default(false)
            }
        }
    },
    handler: (request, response) => {
        if(request.query.serviced == true) {
            var statement = "SELECT patients.information, patients.timestamp, patients.type, META(patients).id FROM `" + bucket._name + "` AS patients WHERE patients.type = 'patient' AND ANY note IN patients.notes SATISFIES note.doctor = $id END";
        } else {
            var statement = "SELECT patients.information, patients.timestamp, patients.type, META(patients).id FROM `" + bucket._name + "` AS doctors JOIN `" + bucket._name + "` AS patients ON KEYS doctors.patients WHERE doctors.type = 'doctor' AND META(doctors).id = $id";
        }
        var query = N1qlQuery.fromString(statement);
        bucket.query(query, { "id": request.params.doctorid }, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(result);
        });
    }
});

server.route({
    method: "GET",
    path: "/doctor/appointments/{doctorid}",
    config: {
        validate: {
            params: {
                doctorid: Joi.string().required()
            }
        }
    },
    handler: (request, response) => {
        var statement = "SELECT `" + bucket._name + "`.* FROM `" + bucket._name + "` WHERE type = 'appointment' AND doctor = $id";
        var query = N1qlQuery.fromString(statement);
        bucket.query(query, { "id": request.params.doctorid }, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(result);
        });
    }
});

server.route({
    method: "POST",
    path: "/doctor",
    config: {
        validate: {
            payload: {
                information: {
                    firstname: Joi.string().required(),
                    lastname: Joi.string().required(),
                    gender: Joi.string().required()
                },
                department: Joi.string().required(),
                type: Joi.any().forbidden().default("doctor"),
                timestamp: Joi.any().forbidden().default((new Date()).getTime())
            }
        }
    },
    handler: (request, response) => {
        bucket.insert(UUID.v4(), request.payload, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(request.payload);
        });
    }
});

server.route({
    method: "PUT",
    path: "/doctor/patient",
    config: {
        validate: {
            payload: {
                doctor: Joi.string().required(),
                patient: Joi.string().required()
            }
        }
    },
    handler: (request, response) => {
        bucket.mutateIn(request.payload.doctor, 0, 0).arrayAddUnique("patients", request.payload.patient, true).execute((error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(request.payload);
        });
    }
});

server.route({
    method: "GET",
    path: "/appointments",
    handler: (request, response) => {
        var statement = "SELECT META().id, `" + bucket._name + "`.* FROM `" + bucket._name + "` WHERE type = 'appointment'";
        var query = N1qlQuery.fromString(statement);
        bucket.query(query, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(result);
        });
    }
});

server.route({
    method: "POST",
    path: "/appointment",
    config: {
        validate: {
            payload: {
                doctor: Joi.string().required(),
                patient: Joi.string().required(),
                appointment: Joi.number().required(),
                type: Joi.any().forbidden().default("appointment"),
                timestamp: Joi.any().forbidden().default((new Date()).getTime())
            }
        }
    },
    handler: (request, response) => {
        bucket.insert(UUID.v4(), request.payload, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(request.payload);
        });
    }
});

server.route({
    method: "DELETE",
    path: "/appointment",
    config: {
        validate: {
            payload: {
                appointmentid: Joi.string().required()
            }
        }
    },
    handler: (request, response) => {
        var statement = "DELETE FROM `" + bucket._name + "` WHERE type = 'appointment' AND META().id = $id RETURNING *";
        var query = N1qlQuery.fromString(statement);
        bucket.query(query, { "id": request.payload.appointmentid }, (error, result) => {
            if(error) {
                return response({"code": error.code, "message": error.message}).code(500);
            }
            return response(result);
        });
    }
});

server.start(error => {
    if(error) {
        throw error;
    }
    console.log("Listening at " + server.info.uri);
});