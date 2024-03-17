package com.klaimz.repo;


import com.klaimz.model.Claim;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.mongodb.annotation.MongoFilter;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;

import java.util.List;

@MongoRepository
public interface ClaimRepository extends CrudRepository<Claim, String>, JpaSpecificationExecutor<Claim> {


//    find all query that automatically join the user table to get the user details
    @Join(value = "requester")
    @Join(value = "evaluator")
    @Join(value = "claimManager")
    List<Claim> findAll();


    class Specification {
        public static PredicateSpecification<Claim> findByField(String field, String value) {
            return (root, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get(field), value);
        }
    }
}
