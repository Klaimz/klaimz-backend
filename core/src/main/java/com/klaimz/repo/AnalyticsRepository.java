package com.klaimz.repo;

import com.klaimz.model.ChartEntry;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.mongodb.repository.MongoQueryExecutor;
import io.micronaut.data.repository.CrudRepository;


@MongoRepository
public interface AnalyticsRepository extends CrudRepository<ChartEntry, String>, MongoQueryExecutor<ChartEntry> {

}
