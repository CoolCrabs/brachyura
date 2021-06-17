package io.github.coolcrabs.brachyura.util;

import java.security.MessageDigest;

public class MessageDigestUtil {
    private MessageDigestUtil() { }

    public static final String SHA1 = "SHA-1";
    public static final String SHA256 = "SHA-256";

    static final String HEXES = "0123456789ABCDEF";

    public static MessageDigest messageDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    // https://www.rgagnon.com/javadetails/java-0596.html
    public static String toHexHash(byte[] hash) {
        if (hash == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * hash.length);
        for (final byte b : hash) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }
}
