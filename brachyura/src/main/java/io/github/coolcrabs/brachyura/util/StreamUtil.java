package io.github.coolcrabs.brachyura.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class StreamUtil {
    private StreamUtil() {
    }

    public static String readFullyAsString(InputStream inputStream) {
        try {
            return readFully(inputStream).toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw Util.sneak(e);
        }
    }

    public static byte[] readFullyAsBytes(InputStream inputStream) {
        return readFully(inputStream).toByteArray();
    }

    // https://stackoverflow.com/a/10505933
    private static ByteArrayOutputStream readFully(InputStream inputStream) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            return baos;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

}
