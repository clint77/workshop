package com.couchbase.workshopapp.entity;

import com.couchbase.client.java.repository.annotation.Id;
import org.springframework.data.annotation.Version;

public class Person {
	@Id
	public String id;

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