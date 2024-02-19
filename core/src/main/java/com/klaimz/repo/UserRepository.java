package com.klaimz.repo;


import com.klaimz.model.User;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@MongoRepository
public interface UserRepository extends CrudRepository<User, String> {

    Optional<User> findByEmail(String email);

}
