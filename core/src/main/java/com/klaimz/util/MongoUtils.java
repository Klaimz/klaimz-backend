package com.klaimz.util;

import com.klaimz.model.Claim;
import com.klaimz.model.User;
import com.klaimz.model.api.ChartAnalyticsRequest;
import com.klaimz.model.api.Filter;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.function.BiFunction;

import static com.mongodb.client.model.Aggregates.unwind;

public final class MongoUtils {

    public static final String MATCH = "$match";
    public static final String FIRST = "$first";
    public static final String TO_DOUBLE = "$toDouble";
    public static final String GROUP = "$group";

    public static final String UNWIND = "$unwind";
    public static final String LOOKUP = "$lookup";
    private static final Map<Class<?>, BiFunction<String, String, BsonValue>> classToMatchId = new HashMap<>();


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
        return bson(MATCH, match(filters, clazz));
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
        accumulators.add(Accumulators.first("xvalue", groupBy));
        accumulators.add(Accumulators.sum("yvalue", aggregateBy));

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
        List<Bson> pipeline = new ArrayList<>();

        Bson matchStage = aggregateMatch(filters, Claim.class);
        pipeline.add(matchStage);

        // $lookup and $unwind stages
        Arrays.stream(Claim.class.getDeclaredFields())
                .filter(field -> field.getType().equals(User.class))
                .forEach(field -> {
                    String fieldName = field.getName();

                    BsonDocument lookupStage = bson("$lookup", new BsonDocument()
                            .append("from", bsonV("user"))
                            .append("localField", bsonV(fieldName + "._id"))
                            .append("foreignField", bsonV("_id"))
                            .append("as", bsonV(fieldName)));
                    pipeline.add(lookupStage);

                    BsonDocument unwindStage = bson("$unwind", new BsonDocument()
                            .append("path", bsonV("$" + fieldName))
                            .append("preserveNullAndEmptyArrays", bsonV(true)));
                    pipeline.add(unwindStage);
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
