package com.klaimz.repo;

import com.klaimz.model.Product;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;


@MongoRepository
public interface ProductRepository extends CrudRepository<Product, String> {
    List<Product> findByUid(String uid);
    List<Product> findByBatchNumber(String batchNumber);
}
