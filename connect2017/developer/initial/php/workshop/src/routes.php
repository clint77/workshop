<?php

use Slim\Http\Request;
use Slim\Http\Response;

use Ramsey\Uuid\Uuid;
use League\JsonGuard\Validator;

use Couchbase\N1qlQuery;
use Couchbase\SearchQuery;

function validateJson($data, $schema) {
    $validator = new Validator(
        json_decode(json_encode($data)), // coerce array to object
        json_decode($schema)
    );
    if ($validator->fails()) {
        // $this->logger->debug(json_encode($validator->errors()));
        $messages = array_map(function($e) { return $e->getMessage(); }, $validator->errors());
        throw new InvalidArgumentException(join(' ', $messages));
    }
}

// Crafting the error like the SDK returns, to make indistinguishable
// the case of missing document, and present document but with incorrect type.
define('NOT_FOUND_ERROR_DATA', ['code' => COUCHBASE_KEY_ENOENT, 'message' => "LCB_KEY_ENOENT: The key does not exist on the server"]);

function nowMs() {
    return (int)(microtime(true) * 1000);
}

function newUuid() {
    return Uuid::uuid4()->toString();
}

$app->post('/doctor', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->post('/appointment', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->post('/patient', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->get('/doctor/{id}', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->get('/patient/{id}', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->get('/patients', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->get('/doctors', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->get('/appointments', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->get('/patient/appointments/{patientid}', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->get('/doctor/appointments/{doctorid}', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->delete('/appointment', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->get('/doctor/patients/{doctorid}', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->put('/patient/notes/{patientid}', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->put('/doctor/patient', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});

$app->post('/patients/condition', function (Request $request, Response $response, array $args) {
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
    return $response->withJson((object)[]);
});
