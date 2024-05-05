package com.klaimz.repo;

import com.klaimz.model.SystemProperties;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;


@MongoRepository
public interface SysPropertiesRepo extends CrudRepository<SystemProperties, String> {
}
