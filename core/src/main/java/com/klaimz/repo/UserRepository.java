package com.klaimz.repo;


import com.klaimz.model.User;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.mongodb.annotation.MongoFindQuery;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@MongoRepository
public interface UserRepository extends CrudRepository<User, String>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    @MongoFindQuery("{'roles': {'$in': [:role]}}")
    List<User> findByRoleIn(String role);

    class Specification {
        public static PredicateSpecification<User> findByField(String field, String value) {
            return (root, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get(field), value);
        }
    }

}
