package com.couchbase.workshopapp.repository;

import com.couchbase.workshopapp.entity.Person;
import org.springframework.data.couchbase.core.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends CrudRepository<Person, String> {
	@Query("#{#n1ql.selectEntity} WHERE #{#n1ql.filter}")
	Iterable<Person> findAllPeople();
}