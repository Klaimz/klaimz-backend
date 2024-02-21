package com.klaimz.util;

import java.util.function.Function;

public final class StringUtils {
    public static String VERIFIED = "verified";

    public static  <T> Function<T, String> emptyCheck(Function<T, String> generator, String message) {
        return user -> {
            var value = generator.apply(user);
            if (value == null || value.isBlank()) {
                return message;
            }
            return VERIFIED;
        };
    }

    // check if string is currency
    public static boolean isCurrency(String str) {
        return str.matches("^[0-9]+(\\.[0-9]{1,2})?$");
    }

}
