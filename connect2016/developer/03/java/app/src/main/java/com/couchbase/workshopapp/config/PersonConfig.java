package com.couchbase.workshopapp.config;

import com.couchbase.workshopapp.service.PersonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;


@Configuration
@EnableCouchbaseRepositories
public class PersonConfig {
	@Bean
	public PersonService personService() {
		return new PersonService();
	}
}