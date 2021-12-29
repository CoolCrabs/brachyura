package io.github.coolcrabs.brachyura.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class StreamUtil {
    private StreamUtil() {
    }
    
    public static void copy(InputStream is, OutputStream os) {
        try {
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw Util.sneak(e);
        }
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
            copy(inputStream, baos);
            return baos;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

}
