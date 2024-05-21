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
    public static final List<String> userFields = Arrays
            .stream(Claim.class.getDeclaredFields())
            .filter(field -> field.getType().equals(User.class))
            .map(field -> field.getName())
            .toList();


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
        var groupBy = bsonV("$" + request.getGroupBy());

        if (isOfUser(request.getGroupBy()) && request.getGroupBy().contains("._id")) {
            // if the group by field is a user id, convert the object ID to string
            groupBy = bson("$toString", bsonV("$" + request.getGroupBy()));
        }

        var aggregateBy = bson(TO_DOUBLE, bsonV("$" + request.getAggregateBy()));


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

        if (isOfUser(field) && field.contains("._id")) {
            return new BsonObjectId(new ObjectId(value));
        }
        return new BsonString(value);
    }

    public static boolean isOfUser(String matchField) {
        return userFields.stream().anyMatch(matchField::contains);
    }

    public static String getFieldFromUser(String matchField) {
        return userFields.stream()
                .filter(matchField::contains)
                .findAny()
                .orElse(null);
    }


    public static BsonValue matchIdUser(String field, String value) {
        return new BsonString(value);
    }


    public static List<Bson> filterForClaims(List<Filter> filters) {
        var pipeline = new ArrayList<Bson>();

        // $lookup and $unwind stages
        Arrays.stream(Claim.class.getDeclaredFields())
                .filter(field -> field.getType().equals(User.class))
                .forEach(field -> pipeline.addAll(userJoinPipelineSteps(field.getName())));

        var matchStage = aggregateMatch(filters, Claim.class);  // match stage should be the last stage!
        pipeline.add(matchStage);

        return pipeline;
    }

    public static List<Bson> userJoinPipelineSteps(String fieldName) {
        var pipeline = new ArrayList<Bson>();
        pipeline.add(Aggregates.lookup("user", fieldName + "._id", "_id", fieldName));

        var unwindOptions = new UnwindOptions();
        unwindOptions.preserveNullAndEmptyArrays(true);
        // Add unwind stage to the pipeline
        pipeline.add(Aggregates.unwind("$" + fieldName, unwindOptions));

        return pipeline;
    }

    public static ArrayList<Bson> createAnalyticsPipeline(ChartAnalyticsRequest request) {
        var pipeline = new ArrayList<Bson>();

        Optional.ofNullable(request.getFilters())
                .filter(filters -> !filters.isEmpty())
                .ifPresent(filters -> pipeline.add(aggregateMatch(filters, Claim.class)));


        // If the groupBy field is user field like 'requester.displayName', add a $lookup stage to join with the User collection
        if (isOfUser(request.getGroupBy())) {
            var field = getFieldFromUser(request.getGroupBy());
            pipeline.addAll(userJoinPipelineSteps(field));
        }

        if (request.getGroupBy().startsWith("products.")) {
            pipeline.add(unwind("$products"));
        }

        pipeline.add(group(request));
        return pipeline;
    }
}
