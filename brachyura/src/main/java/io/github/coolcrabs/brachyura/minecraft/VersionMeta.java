package io.github.coolcrabs.brachyura.minecraft;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import static io.github.coolcrabs.brachyura.util.NetUtil.*;

public class VersionMeta {
    JsonElement json;

    public VersionMeta(String url) {
        new VersionMeta(inputStream(url(url)));
    }

    public VersionMeta(InputStream stream) {
        json = JsonParser.parseReader(new InputStreamReader(stream));
    }

    public Download getDownload(String download) {
        return (new Gson()).fromJson(json.getAsJsonObject().get("downloads").getAsJsonObject().get(download), Download.class);
    }

    public class Download {
        public String sha1;
        public int size;
        public String url;
    }
}
