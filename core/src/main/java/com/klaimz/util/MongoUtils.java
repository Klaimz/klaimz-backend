package com.klaimz.util;

import com.klaimz.model.Claim;
import com.klaimz.model.User;
import com.klaimz.model.api.ChartAnalyticsRequest;
import com.klaimz.model.api.Filter;
import com.mongodb.client.model.*;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.function.BiFunction;

import static com.mongodb.client.model.Aggregates.unwind;

public final class MongoUtils {

    public static final String TO_DOUBLE = "$toDouble";

    private static final Map<Class<?>, BiFunction<String, String, BsonValue>> classToMatchId = new HashMap<>();
    public static final String X = "x";
    public static final String Y = "y";


    static {
        classToMatchId.put(Claim.class, MongoUtils::matchIdClaim);
        classToMatchId.put(ObjectId.class, MongoUtils::matchIdUser);
    }


    public static BsonDocument bson(String key, BsonValue value) {
        return new BsonDocument(key, value);
    }

    public static BsonDocument bson(String key, Bson value) {
        return new BsonDocument(key, value.toBsonDocument());
    }

    public static BsonDocument bson(String key, BsonDocument value) {
        return new BsonDocument(key, value);
    }


    public static BsonValue bsonV(Object value) {
        if (value instanceof Number) {
            return new BsonDouble(Double.parseDouble(value.toString()));
        } else if (value instanceof Boolean) {
            return new BsonBoolean((Boolean) value);
        }
        return new BsonString(value.toString());
    }

    public static Bson aggregateMatch(List<Filter> filters, Class<?> clazz) {
        return Aggregates.match(match(filters, clazz));
    }


    public static Bson match(List<Filter> filters, Class<?> clazz) {
        BsonDocument matchBody = new BsonDocument();
        var matchFunction = classToMatchId.get(clazz);
        filters.forEach(filter -> matchBody.append(filter.getField(),
                matchFunction.apply(filter.getField(), filter.getValue())
        ));
        return matchBody;
    }

    public static Bson group(ChartAnalyticsRequest request) {
        String groupBy = "$" + request.getGroupBy();
        Bson aggregateBy = bson(TO_DOUBLE, bsonV("$" + request.getAggregateBy()));


        var accumulators = new ArrayList<BsonField>();
        accumulators.add(Accumulators.first(X, groupBy));

        switch (request.getAggregateType()) {
            case "avg" -> accumulators.add(Accumulators.avg(Y, aggregateBy));
            case "count" -> accumulators.add(Accumulators.sum(Y, new BsonInt32(1)));
            case "max" -> accumulators.add(Accumulators.max(Y, aggregateBy));
            case "min" -> accumulators.add(Accumulators.min(Y, aggregateBy));

            default -> accumulators.add(Accumulators.sum(Y, aggregateBy));
        }

        return Aggregates.group(groupBy, accumulators);
    }

    public static BsonValue matchIdClaim(String field, String value) {
        var idList = List.of("evaluator._id", "requester._id", "claimManager._id");

        if (idList.contains(field)) {
            return new BsonObjectId(new ObjectId(value));
        }
        return new BsonString(value);
    }


    public static BsonValue matchIdUser(String field, String value) {
        return new BsonString(value);
    }


    public static List<Bson> filterForClaims(List<Filter> filters) {
        var pipeline = new ArrayList<Bson>();

        var matchStage = aggregateMatch(filters, Claim.class);
        pipeline.add(matchStage);

        // $lookup and $unwind stages
        Arrays.stream(Claim.class.getDeclaredFields())
                .filter(field -> field.getType().equals(User.class))
                .forEach(field -> {
                    var fieldName = field.getName();

                    // Add lookup stage to the pipeline
                    pipeline.add(Aggregates.lookup("user", fieldName + "._id", "_id", fieldName));

                    var unwindOptions = new UnwindOptions();
                    unwindOptions.preserveNullAndEmptyArrays(true);
                    // Add unwind stage to the pipeline
                    pipeline.add(Aggregates.unwind("$" + fieldName, unwindOptions));
                });

        return pipeline;
    }

    public static ArrayList<Bson> createAnalyticsPipeline(ChartAnalyticsRequest request) {
        var pipeline = new ArrayList<Bson>();

        Optional.ofNullable(request.getFilters())
                .filter(filters -> !filters.isEmpty())
                .ifPresent(filters -> pipeline.add(aggregateMatch(filters, Claim.class)));


        if (request.getGroupBy().startsWith("products.")) {
            pipeline.add(unwind("$products"));
        }

        pipeline.add(group(request));
        return pipeline;
    }
}
