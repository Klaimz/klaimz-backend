package com.klaimz.repo;

import com.klaimz.model.Role;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import org.bson.types.ObjectId;

import java.util.List;


@MongoRepository
public interface RoleRepository extends CrudRepository<Role, String> {
    List<Role> findByKeyIn(List<String> keys);
}
