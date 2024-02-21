package com.klaimz.repo;

import com.klaimz.model.ClaimTemplate;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;
import org.bson.types.ObjectId;


@MongoRepository
public interface ClaimTemplateRepository extends CrudRepository<ClaimTemplate, String>, JpaSpecificationExecutor<ClaimTemplate> {
}
