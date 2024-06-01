package com.klaimz.util;

import com.klaimz.model.ChartEntry;
import com.klaimz.model.Claim;
import com.klaimz.model.FilterableRequest;
import com.klaimz.model.User;
import com.klaimz.model.api.ChartAnalyticsRequest;
import com.klaimz.model.api.Filter;
import com.klaimz.model.api.TopKClaimRequest;
import com.mongodb.client.model.*;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class MongoUtils {

    public static final String TO_DOUBLE = "$toDouble";

    private static final Map<Class<?>, BiFunction<String, String, Bson>> classToMatchId = new HashMap<>();
    public static final String X = "x";
    public static final String Y = "y";
    public static final List<String> USER_FIELDS = Arrays
            .stream(Claim.class.getDeclaredFields())
            .filter(field -> field.getType().equals(User.class))
            .map(Field::getName)
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
        filters.forEach(filter -> {
                    var bsonFilter = matchFunction.apply(filter.getField(), filter.getValue()).toBsonDocument();
                    var key = bsonFilter.getFirstKey();
                    matchBody.append(key, bsonFilter.get(key));
        });
        return matchBody;
    }

    public static Bson group(ChartAnalyticsRequest request) {
        var groupBy = bsonV("$" + request.getGroupBy());

        if (isOfUser(List.of(request.getGroupBy())) && request.getGroupBy().contains("._id")) {
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

    public static List<ChartEntry> convertToPie(List<ChartEntry> data) {
        var total = data.stream().mapToDouble(ChartEntry::getY).sum();
        for (var entry : data) {
            double yRounded = Math.round(entry.getY() * 1000000.0 / total) / 10000.0;
            entry.setY(yRounded);
        }
        return data;
    }

    public static Bson matchIdClaim(String field, String value) {

        if (isOfUser(List.of(field)) && field.contains("._id")) {
            return bson(field, new BsonObjectId(new ObjectId(value)));
        }

        if (field.startsWith("products.")) {
            return bson("products", bson("$elemMatch", bson(field.replace("products.", ""), bsonV(value))));
        }
        return bson(field, new BsonString(value));
    }

    public static boolean isOfUser(List<String> matchField) {
        return matchField.stream()
                .anyMatch(field -> USER_FIELDS.stream().anyMatch(field::contains));
    }

    public static List<String> findUserFields(List<String> matchField) {
        return matchField.stream()
                .map(field -> USER_FIELDS.stream().filter(field::contains).findAny())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }


    public static Bson matchIdUser(String field, String value) {
        return bson(field,new BsonString(value));
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

    private static ArrayList<Bson> createFilterablePipeline(FilterableRequest request) {
        if (request.getFields() == null || request.getFields().isEmpty()) {
            return new ArrayList<>();
        }

        var filterFields = request.getFields();

        var userFilterFields = findUserFields(filterFields);
        var pipeline = userFilterFields.stream()
                .map(MongoUtils::userJoinPipelineSteps)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(ArrayList::new));


        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            pipeline.add(aggregateMatch(request.getFilters(), Claim.class));
        }
        return pipeline;
    }

    public static ArrayList<Bson> createAnalyticsPipeline(ChartAnalyticsRequest request) {
        var pipeline = createFilterablePipeline(request);
        pipeline.add(group(request));

        return pipeline;
    }

    public static ArrayList<Bson> createGetTopKClaimsPipeline(TopKClaimRequest request) {
        var pipeline = createFilterablePipeline(request);

        // Add a sort stage to the pipeline
        pipeline.add(Aggregates.sort(Sorts.descending(request.getSortBy())));

        // Add a limit stage to the pipeline
        pipeline.add(Aggregates.limit(request.getLimit()));

        // create a projection stage to project the target field and the sortBy field into the ChartEntry model
        var projection = new BsonDocument();
        projection.append("_id", new BsonString("$" + request.getTarget()));
        projection.append(X, bsonV("$" + request.getTarget()));
        projection.append(Y, bson(TO_DOUBLE, bsonV("$" + request.getSortBy())));

        pipeline.add(Aggregates.project(projection));

        return pipeline;
    }
}
