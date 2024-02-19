package com.klaimz.repo;

import com.klaimz.model.LoginData;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@MongoRepository
public interface LoginRepository extends CrudRepository<LoginData, String> {

    Optional<LoginData> findByToken(String token);
}
