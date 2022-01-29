package io.github.coolcrabs.brachyura.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MessageDigestUtil {
    private MessageDigestUtil() { }

    public static final String SHA1 = "SHA-1";
    public static final String SHA256 = "SHA-256";

    static final String HEXES = "0123456789ABCDEF";
    static final String LOWER_HEXES = "0123456789abcdef";

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

    public static String toLowerCaseHexHash(byte[] hash) {
        if (hash == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * hash.length);
        for (final byte b : hash) {
            hex.append(LOWER_HEXES.charAt((b & 0xF0) >> 4)).append(LOWER_HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static void update(MessageDigest md, String string) {
        if (string != null) {
            md.update(string.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void update(MessageDigest md, int i) {
        md.update(
            new byte[] {
                (byte)(i >>> 24),
                (byte)(i >>> 16),
                (byte)(i >>> 8),
                (byte)i
            }
        );
    }

    public static void update(MessageDigest md, long i) {
        md.update(
            new byte[] {
                (byte)(i >>> 56),
                (byte)(i >>> 48),
                (byte)(i >>> 40),
                (byte)(i >>> 32),
                (byte)(i >>> 24),
                (byte)(i >>> 16),
                (byte)(i >>> 8),
                (byte)i
            }
        );
    }
}
