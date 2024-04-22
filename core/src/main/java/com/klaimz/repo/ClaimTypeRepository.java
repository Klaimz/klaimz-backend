package com.klaimz.repo;


import com.klaimz.model.ClaimType;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

@MongoRepository
public interface ClaimTypeRepository extends CrudRepository<ClaimType, String> { }
