package com.klaimz.repo;


import com.klaimz.model.Claim;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.repository.jpa.JpaSpecificationExecutor;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;

@MongoRepository
@Join(value = "requester")
@Join(value = "evaluator")
@Join(value = "claimManager")
public interface ClaimRepository extends CrudRepository<Claim, String>, JpaSpecificationExecutor<Claim> {

    class Specification {
        public static PredicateSpecification<Claim> findByField(String field, String value) {
            return (root, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get(field), value);
        }
    }
}
