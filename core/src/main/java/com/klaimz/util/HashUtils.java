package com.klaimz.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class HashUtils {

    public static final String SHA_256 = "SHA-256";

    public static String hash(String input) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
        messageDigest.update(input.getBytes());
        return new String(Base64.getEncoder().encode(messageDigest.digest()));
    }
}
