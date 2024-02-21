package com.klaimz.repo;

import com.klaimz.model.FormField;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;


@MongoRepository
public interface FormFieldRepository extends CrudRepository<FormField, String>, JpaSpecificationExecutor<FormField> {
}
