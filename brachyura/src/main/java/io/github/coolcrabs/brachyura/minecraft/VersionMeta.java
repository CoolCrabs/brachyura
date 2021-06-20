package io.github.coolcrabs.brachyura.minecraft;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.coolcrabs.brachyura.util.OsUtil;

import static io.github.coolcrabs.brachyura.util.NetUtil.*;

public class VersionMeta {
    JsonElement json;

    VersionMeta(String url) {
        new VersionMeta(inputStream(url(url)));
    }

    VersionMeta(InputStream stream) {
        json = JsonParser.parseReader(new InputStreamReader(stream));
    }

    Download getDownload(String download) {
        return (new Gson()).fromJson(json.getAsJsonObject().get("downloads").getAsJsonObject().get(download), Download.class);
    }

    List<Dependency> getDependencies() {
        Gson gson = new Gson();
        ArrayList<Dependency> result = new ArrayList<>();
        JsonArray libraries = json.getAsJsonObject().get("libraries").getAsJsonArray();
        for (int i = 0; i < libraries.size(); i++) {
            JsonObject library = libraries.get(i).getAsJsonObject();
            if (!Rules.allowed(library.get("rules"))) continue;
            Dependency dependency = new Dependency();
            dependency.name = library.get("name").getAsString();
            dependency.downloads = new ArrayList<>();
            boolean hasNatives = false;
            String natives = null;
            if (library.get("natives") != null) {
                JsonElement native0 = library.get("natives").getAsJsonObject().get(OsUtil.OS.mojang);
                if (native0 != null) {
                    hasNatives = true;
                    natives = native0.getAsString();
                }
            }
            JsonObject downloads = library.get("downloads").getAsJsonObject();
            if (downloads.get("artifact") != null) {
                dependency.downloads.add(gson.fromJson(downloads.get("artifact"), DependencyDownload.class));
            }
            if (downloads.get("classifiers") != null) {
                JsonObject classifiers = downloads.get("classifiers").getAsJsonObject();
                if (hasNatives) {
                    dependency.downloads.add(gson.fromJson(classifiers.get(natives), DependencyDownload.class));
                }
                if (classifiers.get("sources") != null) {
                    dependency.downloads.add(gson.fromJson(classifiers.get("sources"), DependencyDownload.class));
                }
            }
            result.add(dependency);
        }
        return result;
    }

    class Download {
        String sha1;
        int size;
        String url;
    }

    class DependencyDownload {
        String path;
        String sha1;
        String size;
        String url;
    }

    class Dependency {
        String name;
        List<DependencyDownload> downloads;
    }
}
