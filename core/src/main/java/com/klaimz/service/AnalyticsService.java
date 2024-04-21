package com.klaimz.service;


import com.klaimz.model.ChartEntry;
import com.klaimz.model.api.ChartAnalyticsRequest;
import com.klaimz.model.api.Filter;
import com.klaimz.repo.AnalyticsRepository;
import io.micronaut.data.mongodb.operations.options.MongoAggregationOptions;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.BsonDocument;
import org.bson.BsonObjectId;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;

import static com.klaimz.util.Constants.*;
import static com.klaimz.util.StringUtils.bson;
import static com.klaimz.util.StringUtils.bsonV;

@Singleton
public class AnalyticsService {

    @Inject
    AnalyticsRepository analyticsRepository;


    private BsonValue getMatchId(String field, String value) {
        var idList = List.of("evaluator._id", "requester._id", "claimManager._id");
        var numberList = List.of("amount");

        if (idList.contains(field)) {
            return new BsonObjectId(new ObjectId(value));
        }

        return new BsonString(value);
    }


    public List<ChartEntry> getChartAnalytics(ChartAnalyticsRequest request) {
        var pipeline = new ArrayList<Bson>();

        Optional.ofNullable(request.getFilters())
                .filter(filters -> !filters.isEmpty())
                .ifPresent(filters -> pipeline.add(createMatch(filters)));

        pipeline.add(createGroup(request));

        MongoAggregationOptions aggregationOptions = new MongoAggregationOptions();

        return analyticsRepository.findAll(pipeline, aggregationOptions);
    }

    private Bson createMatch(List<Filter> filters) {


        BsonDocument matchBody = new BsonDocument();
        filters.forEach(filter -> matchBody.append(filter.getField(),
                getMatchId(filter.getField(), filter.getValue())
        ));
        return bson(MATCH, matchBody);
    }

    private Bson createGroup(ChartAnalyticsRequest request) {
        BsonDocument group = new BsonDocument();
        BsonString groupBy = new BsonString("$" + request.getGroupBy());
        group.append("_id", groupBy);
        group.append("xvalue", bson(FIRST, groupBy));
        group.append("yvalue", bson("$" + request.getAggregateType(), bson(TO_DOUBLE, bsonV("$" + request.getAggregateBy()))));
        return bson(GROUP, group);
    }

}
