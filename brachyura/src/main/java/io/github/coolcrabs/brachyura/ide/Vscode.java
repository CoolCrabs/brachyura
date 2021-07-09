package io.github.coolcrabs.brachyura.ide;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Matcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class Vscode {
    private Vscode() { }

    public static void updateSettingsJson(Path settingsJsonFile, List<JavaJarDependency> dependencies) {
        Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
        JsonElement settingsJson = null;
        if (Files.isRegularFile(settingsJsonFile)) {
            try {
                settingsJson = gson.fromJson(PathUtil.newBufferedReader(settingsJsonFile), JsonElement.class);
            } catch (Exception e) {
                //too bad
            }
        }
        if (settingsJson == null || !settingsJson.isJsonObject()) {
            settingsJson = new JsonObject();
        }
        JsonObject referencedLibraries = new JsonObject();
        settingsJson.getAsJsonObject().add("java.project.referencedLibraries", referencedLibraries);
        JsonArray include = new JsonArray();
        referencedLibraries.add("include", include);
        JsonObject sources = new JsonObject();
        referencedLibraries.add("sources", sources);
        for (JavaJarDependency dependency : dependencies) {
            String jar = dependency.jar.toAbsolutePath().toString();
            String source = dependency.sourcesJar == null ? null : dependency.sourcesJar.toAbsolutePath().toString();
            include.add(jar);
            sources.addProperty(Matcher.quoteReplacement(jar), source);
        }
        PathUtil.deleteIfExists(settingsJsonFile);
        try {
            try (BufferedWriter writer = PathUtil.newBufferedWriter(settingsJsonFile, StandardOpenOption.CREATE)) {
                gson.toJson(settingsJson, writer);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static void updateLaunchJson(Path launchJsonFile, LaunchJson launchJson) {
        PathUtil.deleteIfExists(launchJsonFile);
        try {
            try (BufferedWriter writer = PathUtil.newBufferedWriter(launchJsonFile, StandardOpenOption.CREATE)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(launchJson, writer);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public static class LaunchJson {
        public final String version = "0.2.0";
        public Configuration[] configurations;

        public static class Configuration {
            public String type = "java";
            public String name;
            public String request = "launch";
            public String cwd;
            public String console = "internalConsole";
            public String mainClass;
            public String vmArgs = "";
            public String args = "";
            public boolean stopOnEntry = false;
            public String[] classPaths = new String[] {
                "$Auto",
                "${workspaceFolder}/src/main/resources/"
            };
        }
    }
}
