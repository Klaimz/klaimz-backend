package com.klaimz.util;

import com.klaimz.model.Claim;
import com.klaimz.model.User;
import com.klaimz.model.api.ChartAnalyticsRequest;
import com.klaimz.model.api.Filter;
import org.bson.*;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;

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
        BsonDocument group = new BsonDocument();
        BsonString groupBy = new BsonString("$" + request.getGroupBy());
        group.append("_id", groupBy);
        group.append("xvalue", bson(FIRST, groupBy));
        group.append("yvalue", bson("$" + request.getAggregateType(), bson(TO_DOUBLE, bsonV("$" + request.getAggregateBy()))));

        return bson(GROUP, group);
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
                    pipeline.add(createLookupStage(field.getName()));
                    pipeline.add(createUnwindStage(field.getName()));
                });

        return pipeline;
    }

    private static Bson createLookupStage(String fieldName) {
        return bson(LOOKUP, new BsonDocument()
                .append("from", bsonV("user"))
                .append("localField", bsonV(fieldName + "._id"))
                .append("foreignField", bsonV("_id"))
                .append("as", bsonV(fieldName)));
    }

    private static Bson createUnwindStage(String fieldName) {
        return bson(UNWIND, new BsonDocument()
                .append("path", bsonV("$" + fieldName))
                .append("preserveNullAndEmptyArrays", bsonV(true)));
    }
}
