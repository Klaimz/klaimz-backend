package com.klaimz.repo;


import com.klaimz.model.Claim;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.repository.MongoQueryExecutor;
import io.micronaut.data.repository.CrudRepository;

@MongoRepository
@Join(value = "requester")
@Join(value = "evaluator")
@Join(value = "claimManager")
public interface ClaimRepository extends CrudRepository<Claim, String>, MongoQueryExecutor<Claim> {

}
