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
    $schema = '
        {
            "type": "object",
            "required": ["department", "information"],
            "additionalProperties": false,
            "properties": {
                "department": {"type": "string"},
                "information": {
                    "type": "object",
                    "required": ["firstname", "lastname", "gender"],
                    "additionalProperties": false,
                    "properties": {
                        "firstname": {"type": "string"},
                        "lastname": {"type": "string"},
                        "gender": {"type": "string"}
                    }
                }
            }
        }
    ';
    $data = $request->getParsedBody();
    try {
        validateJson($data, $schema);
        $data['type'] = 'doctor';
        $data['timestamp'] = nowMs();
        $this->couchbase->insert(newUuid(), $data);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    } catch (InvalidArgumentException $ex) {
        return $response->withJson(['message' => $ex->getMessage()], 500);
    }
    return $response->withJson($data);
});

$app->post('/appointment', function (Request $request, Response $response, array $args) {
    $schema = '
        {
            "type": "object",
            "required": ["appointment", "doctor", "patient"],
            "additionalProperties": false,
            "properties": {
                "appointment": {"type": "number"},
                "doctor": {"type": "string"},
                "patient": {"type": "string"}
            }
        }
    ';
    $data = $request->getParsedBody();
    try {
        validateJson($data, $schema);
        $data['type'] = 'appointment';
        $data['timestamp'] = nowMs();
        $this->couchbase->insert(newUuid(), $data);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    } catch (InvalidArgumentException $ex) {
        return $response->withJson(['message' => $ex->getMessage()], 500);
    }
    return $response->withJson($data);
});

$app->post('/patient', function (Request $request, Response $response, array $args) {
    $schema = '
        {
            "type": "object",
            "required": ["firstname", "lastname", "gender"],
            "additionalProperties": false,
            "properties": {
                "firstname": {"type": "string"},
                "lastname": {"type": "string"},
                "gender": {"type": "string"}
            }
        }
    ';
    $data = $request->getParsedBody();
    try {
        validateJson($data, $schema);
        $data['type'] = 'patient';
        $data['timestamp'] = nowMs();
        $this->couchbase->insert(newUuid(), $data);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    } catch (InvalidArgumentException $ex) {
        return $response->withJson(['message' => $ex->getMessage()], 500);
    }
    return $response->withJson($data);
});

$app->get('/doctor/{id}', function (Request $request, Response $response, array $args) {
    try {
        $result = $this->couchbase->get($args['id']);
        if ($result->value['type'] == 'doctor') {
            return $response->withJson($result->value);
        } else {
            return $response->withJson(NOT_FOUND_ERROR_DATA, 500);
        }
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    }
});

$app->get('/patient/{id}', function (Request $request, Response $response, array $args) {
    try {
        $result = $this->couchbase->get($args['id']);
        if ($result->value['type'] == 'patient') {
            return $response->withJson($result->value);
        } else {
            return $response->withJson(NOT_FOUND_ERROR_DATA, 500);
        }
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    }
});

$app->get('/patients', function (Request $request, Response $response, array $args) {
    $statement = "SELECT META().id, `{$this->bucket}`.* FROM `{$this->bucket}` WHERE type = 'patient'";
    $query = N1qlQuery::fromString($statement);
    try {
        $result = $this->couchbase->query($query);
        return $response->withJson($result->rows);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    }
});

$app->get('/doctors', function (Request $request, Response $response, array $args) {
    $statement = "SELECT META().id, `{$this->bucket}`.* FROM `{$this->bucket}` WHERE type = 'doctor'";
    $query = N1qlQuery::fromString($statement);
    try {
        $result = $this->couchbase->query($query);
        return $response->withJson($result->rows);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    }
});

$app->get('/appointments', function (Request $request, Response $response, array $args) {
    $statement = "SELECT META().id, `{$this->bucket}`.* FROM `{$this->bucket}` WHERE type = 'appointment'";
    $query = N1qlQuery::fromString($statement);
    try {
        $result = $this->couchbase->query($query);
        return $response->withJson($result->rows);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    }
});

$app->get('/patient/appointments/{patientid}', function (Request $request, Response $response, array $args) {
    // Note, that parameter have to be escaped, because we use double quotes. Otherwise PHP will substitute it first.
    $statement = "SELECT META().id, `{$this->bucket}`.* FROM `{$this->bucket}` WHERE type = 'appointment' AND patient = \$id";
    $query = N1qlQuery::fromString($statement);
    $query->namedParams(['id' => $args['patientid']]);
    try {
        $result = $this->couchbase->query($query);
        return $response->withJson($result->rows);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    }
});

$app->get('/doctor/appointments/{doctorid}', function (Request $request, Response $response, array $args) {
    // Note, that parameter have to be escaped, because we use double quotes. Otherwise PHP will substitute it first.
    $statement = "SELECT META().id, `{$this->bucket}`.* FROM `{$this->bucket}` WHERE type = 'appointment' AND doctor = \$id";
    $query = N1qlQuery::fromString($statement);
    $query->namedParams(['id' => $args['doctorid']]);
    try {
        $result = $this->couchbase->query($query);
        return $response->withJson($result->rows);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    }
});

$app->delete('/appointment', function (Request $request, Response $response, array $args) {
    $schema = '
        {
            "type": "object",
            "required": ["appointmentid"],
            "additionalProperties": false,
            "properties": {
                "appointmentid": {"type": "string"}
            }
        }
    ';
    $data = $request->getParsedBody();
    try {
        validateJson($data, $schema);
        // Note, that parameter have to be escaped, because we use double quotes. Otherwise PHP will substitute it first.
        $statement = "DELETE FROM `{$this->bucket}` WHERE type = 'doctor' AND META().id = \$id RETURNING *";
        $query = N1qlQuery::fromString($statement);
        $query->namedParams(['id' => $data['appointmentid']]);
        $result = $this->couchbase->query($query);
        return $response->withJson($result->rows);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    } catch (InvalidArgumentException $ex) {
        return $response->withJson(['message' => $ex->getMessage()], 500);
    }
    return $response->withJson($data);
});

$app->get('/doctor/patients/{doctorid}', function (Request $request, Response $response, array $args) {
    $serviced = boolval($request->getQueryParam('serviced', false));
    if ($serviced) {
        $statement = "SELECT patiens.information, patients.timestamp, patients.type, META(patients).id
                      FROM `{$this->bucket}` AS patients
                      WHERE patients.type = 'patient' AND ANY note IN patients.notes SATISFIES note.doctor = \$id END";
    } else {
        $statement = "SELECT patiens.information, patients.timestamp, patients.type, META(patients).id
                      FROM `{$this->bucket}` AS doctors
                      JOIN `{$this->bucket}` AS patients ON KEYS doctors.patients
                      WHERE doctors.type = 'doctor' AND META(doctors).id = \$id";
    }
    $query = N1qlQuery::fromString($statement);
    $query->namedParams(['id' => $args['doctorid']]);
    try {
        $result = $this->couchbase->query($query);
        return $response->withJson($result->rows);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    }
});

$app->put('/patient/notes/{patientid}', function (Request $request, Response $response, array $args) {
    $schema = '
        {
            "type": "object",
            "required": ["doctor", "message"],
            "additionalProperties": false,
            "properties": {
                "doctor": {"type": "string"},
                "message": {"type": "string"}
            }
        }
    ';
    $data = $request->getParsedBody();
    try {
        validateJson($data, $schema);
        $data['timestamp'] = nowMs();

        $res = $this->couchbase->mutateIn($args['patientid'])
            ->arrayAppend("notes", $data, ['createPath' => true])
            ->execute();
        if ($res->error) {
            return $response->withJson(['code' => $res->error->getCode(), 'message' => $res->error->getMessage()], 500);
        }
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    } catch (InvalidArgumentException $ex) {
        return $response->withJson(['message' => $ex->getMessage()], 500);
    }
    return $response->withJson($data);
});

$app->put('/doctor/patient', function (Request $request, Response $response, array $args) {
    $schema = '
        {
            "type": "object",
            "required": ["doctor", "patient"],
            "additionalProperties": false,
            "properties": {
                "doctor": {"type": "string"},
                "patient": {"type": "string"}
            }
        }
    ';
    $data = $request->getParsedBody();
    try {
        validateJson($data, $schema);

        $res = $this->couchbase->mutateIn($data['doctor'])
            ->arrayAddUnique("patients", $data['patient'], ['createPath' => true])
            ->execute();
        if ($res->error) {
            return $response->withJson(['code' => $res->error->getCode(), 'message' => $res->error->getMessage()], 500);
        }
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    } catch (InvalidArgumentException $ex) {
        return $response->withJson(['message' => $ex->getMessage()], 500);
    }
    return $response->withJson($data);
});

$app->post('/patients/condition', function (Request $request, Response $response, array $args) {
    $schema = '
        {
            "type": "object",
            "required": ["search"],
            "additionalProperties": false,
            "properties": {
                "search": {"type": "string"},
                "fuzziness": {"type": "number"}
            }
        }
    ';

    $data = $request->getParsedBody();
    try {
        validateJson($data, $schema);

        $match = SearchQuery::match($data['search']);
        if ($data['fuzziness']) {
            $match->fuzziness($data['fuzziness']);
        }
        $query = new SearchQuery('medical-condition', $match);
        $query->fields(["information.firstname", "information.lastname", "notes.message"]);
        $query->highlight(SearchQuery::HIGHLIGHT_HTML, "notes.message");
        $result = $this->couchbase->query($query);
        return $response->withJson($result->hits);
    } catch (\Couchbase\Exception $ex) {
        return $response->withJson(['code' => $ex->getCode(), 'message' => $ex->getMessage()], 500);
    }
});
