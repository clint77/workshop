package com.couchbase.workshopapp.web;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.couchbase.client.java.error.TemporaryFailureException;
import com.couchbase.workshopapp.entity.Person;
import com.couchbase.workshopapp.entity.PersonCollection;
import com.couchbase.workshopapp.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PersonController {
	private final PersonService personService;

	@Autowired
	public PersonController(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * Find all people in the person repository
	 */
	@RequestMapping(value = "/api/getAll", method = RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> getAll() {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			PersonCollection personList = new PersonCollection(personService.getAll());
			response.put("status", "ok");
			response.put("results", personList);
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			response.put("status", "failed");
			response.put("error", ex.getMessage());
			ex.printStackTrace();
			if (ex instanceof TemporaryFailureException) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(response);
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(response);
			}
		}
	}

	/**
	 * Get a single person entity
	 */
	@RequestMapping(value="/api/get", method = RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> get(@RequestParam(value = "id")String id){
		Map<String, Object> response = new HashMap<String, Object>();
		if (id == null) {
			response.put("status", "failed");
			response.put("error", "missing person id");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(response);
		}
		try {
			Person person = personService.get(id);
			if (person != null) {
				response.put("status", "ok");
				response.put("results", person);
				return ResponseEntity.ok(response);
			}  else {
				throw new Exception("Document not found");
			}

		} catch (Exception ex) {
			response.put("status", "failed");
			response.put("error", ex.getMessage());
			if (ex instanceof TemporaryFailureException) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(response);
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(response);
			}
		}

	}

	/**
	 * Save a person entity
	 * @param person json body containing the person's information
	 * @return
	 */

	@RequestMapping(value = "/api/save", method = RequestMethod.POST)
	public ResponseEntity<Map<String,Object>> save(@RequestBody Person person) {
		Map<String, Object> response = new HashMap<String, Object>();
		if (person == null) {
			response.put("status", "failed");
			response.put("error", "missing person info");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(response);
		}
		try {
			UUID uuid = UUID.randomUUID();
			person.id = uuid.toString();
			personService.save(person);
			response.put("status", "ok");
			return ResponseEntity.ok(response);

		} catch (Exception ex) {
			response.put("status", "failed");
			response.put("error", ex.getMessage());
			if (ex instanceof TemporaryFailureException) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(response);
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(response);
			}
		}
	}

	/**
	 * Delete a person entity
	 * @param req unique identifier for the person {"id":"1"}
	 * @return
	 */
	@RequestMapping(value = "/api/delete", method = RequestMethod.POST)
	public ResponseEntity<Map<String,Object>> delete(@RequestBody Map<String,String> req) {
		Map<String,Object> response = new HashMap<String, Object>();
		String id = req.get("id");
		if (id == "") {
			response.put("status", "failed");
			response.put("error", "missing id");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(response);
		}
		try{
			personService.delete(id);
			response.put("status","ok");
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			response.put("status", "failed");
			response.put("error", ex.getMessage());
			if (ex instanceof TemporaryFailureException)  {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(response);
			} else {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(response);
			}
		}

	}
}