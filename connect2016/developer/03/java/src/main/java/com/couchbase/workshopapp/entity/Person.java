package com.couchbase.workshopapp.entity;

import com.couchbase.client.java.repository.annotation.Id;
import org.springframework.data.annotation.Version;

/**
 * Person entity for our person repository
 */
public class Person {
	/* Couchbase key */
	@Id
	public String id;

	/* Couchbase document cas */
	@Version
	public long Version;

	public String firstName;

	public String lastName;

	public String email;

	@Override
	public String toString(){
		return  "firstName:" + this.firstName + "," +
				"lastName:" + this.lastName + "," +
				"email:" + this.email + "," +
				"cas:" + this.Version;
	}

}