package com.klaimz.repo;


import com.klaimz.model.Claim;
import com.klaimz.model.User;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import org.bson.types.ObjectId;

@MongoRepository
public interface ClaimRepository extends CrudRepository<Claim, String>, JpaSpecificationExecutor<Claim> {

    class Specification {
        public static PredicateSpecification<Claim> findByField(String field, String value) {
            return (root, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get(field), value);
        }
    }
}
