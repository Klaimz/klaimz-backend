package com.klaimz.util;

import java.util.function.Function;

public final class StringUtils {
    public static String VERIFIED = "verified";

    public static <T> Function<T, String> emptyCheck(Function<T, Object> generator, String message, Function<T, Boolean> shouldCheck) {
        return user -> {
            if (shouldCheck.apply(user)) {
                var value = generator.apply(user);
                if (value == null || (value instanceof String && ((String) value).isBlank())) {
                    return message;
                }
            }
            return VERIFIED;
        };
    }

    public static <T> Function<T, String> emptyCheck(Function<T, Object> generator, String message) {
        return emptyCheck(generator, message, user -> true);
    }

    public static String emptyCheck(String value, String message) {
        return emptyCheck(Function.identity(), message, user -> true).apply(value);
    }

    // check if string is currency
    public static boolean isCurrency(double currency) {
        return Double.toString(currency).matches("^[0-9]+(\\.[0-9]{1,2})?$");
    }

}
