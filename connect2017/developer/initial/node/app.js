const Couchbase = require("couchbase");
const Hapi = require("hapi");
const Joi = require("Joi");
const UUID = require("uuid");

const server = new Hapi.Server();
var N1qlQuery = Couchbase.N1qlQuery;

const cluster = new Couchbase.Cluster("couchbase://localhost");

/*
 * Step 1 - Authenticating with a Role Based Account
 * **************** PUT CODE HERE ******************
 */

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
});

server.route({
    method: "GET",
    path: "/doctors",
    handler: (request, response) => {
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
});

server.route({
    method: "GET",
    path: "/appointments",
    handler: (request, response) => {
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
});

server.start(error => {
    if(error) {
        throw error;
    }
    console.log("Listening at " + server.info.uri);
});