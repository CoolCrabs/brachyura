package io.github.coolcrabs.brachyura.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class GsonUtil {
    private GsonUtil() { }

    public static InputStream toIs(JsonElement e, Gson g) {
        ByteArrayOutputStreamEx os = new ByteArrayOutputStreamEx();
        try {
            try (OutputStreamWriter w = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
                g.toJson(e, w);
            }
        } catch (IOException e0) {
            // Shouldn't be possible
            throw Util.sneak(e0);
        }
        return new ByteArrayInputStream(os.buf(), 0, os.size());
    }
}
