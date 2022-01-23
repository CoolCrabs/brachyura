package io.github.coolcrabs.brachyura.minecraft;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.Nullable;

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

    VMDownload getDownload(String download) {
        return (new Gson()).fromJson(json.getAsJsonObject().get("downloads").getAsJsonObject().get(download), VMDownload.class);
    }

    List<VMDependency> getDependencies() {
        Gson gson = new Gson();
        LinkedHashMap<String, VMDependency> result = new LinkedHashMap<>();
        JsonArray libraries = json.getAsJsonObject().get("libraries").getAsJsonArray();
        for (int i = 0; i < libraries.size(); i++) {
            JsonObject library = libraries.get(i).getAsJsonObject();
            if (!Rules.allowed(library.get("rules"))) continue;
            VMDependency dependency = result.computeIfAbsent(library.get("name").getAsString(), VMDependency::new);
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
                dependency.artifact = gson.fromJson(downloads.get("artifact"), VMDependencyDownload.class);
            }
            if (downloads.get("classifiers") != null) {
                JsonObject classifiers = downloads.get("classifiers").getAsJsonObject();
                if (hasNatives) {
                    dependency.natives = gson.fromJson(classifiers.get(natives), VMDependencyDownload.class);
                }
                if (classifiers.get("sources") != null) {
                    dependency.sources = gson.fromJson(classifiers.get("sources"), VMDependencyDownload.class);
                }
            }
        }
        return new ArrayList<>(result.values());
    }

    VMAssets getVmAssets() {
        return new Gson().fromJson(json.getAsJsonObject().get("assetIndex"), VMAssets.class);
    }

    static class VMAssets {
        String id;
        String sha1;
        int size;
        int totalSize;
        String url;
    }

    static class VMDownload {
        String sha1;
        int size;
        String url;
    }

    static class VMDependencyDownload {
        String path;
        String sha1;
        String size;
        String url;
    }

    static class VMDependency {
        String name;
        @Nullable VMDependencyDownload artifact;
        @Nullable VMDependencyDownload natives;
        @Nullable VMDependencyDownload sources;

        VMDependency(String name) {
            this.name = name;
        }
    }
}
