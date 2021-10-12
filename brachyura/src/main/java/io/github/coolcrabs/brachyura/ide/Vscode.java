package io.github.coolcrabs.brachyura.ide;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeProject.RunConfig;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

public enum Vscode implements Ide {
    INSTANCE;

    @Override
    public String ideName() {
        return "vscode";
    }

    @Override
    public void updateProject(Path projectDir, IdeProject ideProject) {
        try {
            Path vscodeDir = projectDir.resolve(".vscode");
            Files.createDirectories(vscodeDir);
            Path settingsJsonFile = vscodeDir.resolve("settings.json");
            updateSettingsJson(settingsJsonFile, projectDir, ideProject);
            if (!ideProject.runConfigs.isEmpty()) {
                Path launchJsonFile = vscodeDir.resolve("launch.json");
                updateLaunchJson(launchJsonFile, ideProject);
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    // https://github.com/redhat-developer/vscode-java/blob/master/package.json
    void updateSettingsJson(Path settingsJsonFile, Path projectDir, IdeProject ideProject) {
        Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
        JsonObject settingsJson = null;
        if (Files.isRegularFile(settingsJsonFile)) {
            try {
                settingsJson = (JsonObject) gson.fromJson(PathUtil.newBufferedReader(settingsJsonFile), JsonElement.class);
            } catch (Exception e) {
                //too bad
            }
        }
        if (settingsJson == null || !settingsJson.isJsonObject()) {
            settingsJson = new JsonObject();
        }
        settingsJson.addProperty("java.project.outputPath", projectDir.resolve(".brachyura").resolve("vscodeout").toString());
        JsonArray sourcePaths = new JsonArray();
        settingsJson.add("java.project.sourcePaths", sourcePaths);
        for (Path path : ideProject.sourcePaths) {
            sourcePaths.add(projectDir.relativize(path).toString()); // Why does this have to be relative?
        }
        JsonObject referencedLibraries = new JsonObject();
        settingsJson.add("java.project.referencedLibraries", referencedLibraries);
        JsonArray include = new JsonArray();
        referencedLibraries.add("include", include);
        JsonObject sources = new JsonObject();
        referencedLibraries.add("sources", sources);
        for (JavaJarDependency dependency : ideProject.dependencies) {
            include.add(dependency.jar.toString());
            if (dependency.sourcesJar != null) {
                sources.addProperty(dependency.jar.toString(), dependency.sourcesJar.toString());
            }
        }
        try {
            try (AtomicFile atomicFile = new AtomicFile(settingsJsonFile)) {
                try (BufferedWriter writer = PathUtil.newBufferedWriter(atomicFile.tempPath)) {
                    gson.toJson(settingsJson, writer);
                }
                atomicFile.commit();
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    void updateLaunchJson(Path launchJsonFile, IdeProject ideProject) throws IOException {
        try (AtomicFile atomicFile = new AtomicFile(launchJsonFile)) {
            try (JsonWriter jsonWriter = new JsonWriter(PathUtil.newBufferedWriter(atomicFile.tempPath))) {
                jsonWriter.setIndent("  ");
                jsonWriter.beginObject();
                jsonWriter.name("version").value("0.2.0");
                jsonWriter.name("configurations");
                jsonWriter.beginArray();
                for (RunConfig runConfig : ideProject.runConfigs) {
                    jsonWriter.beginObject();
                    jsonWriter.name("type").value("java");
                    jsonWriter.name("name").value(runConfig.name);
                    jsonWriter.name("request").value("launch");
                    jsonWriter.name("cwd").value(runConfig.cwd.toString());
                    jsonWriter.name("console").value("internalConsole");
                    jsonWriter.name("mainClass").value(runConfig.mainClass);
                    jsonWriter.name("vmArgs");
                    jsonWriter.beginArray();
                    for (String vmArg : runConfig.vmArgs) {
                        jsonWriter.value(vmArg);
                    }
                    jsonWriter.endArray();
                    jsonWriter.name("args");
                    jsonWriter.beginArray();
                    for (String arg : runConfig.args) {
                        jsonWriter.value(arg);
                    }
                    jsonWriter.endArray();
                    jsonWriter.name("stopOnEntry").value(false);
                    jsonWriter.name("classPaths");
                    jsonWriter.beginArray();
                    jsonWriter.value(".brachyura/vscodeout");
                    for (Path path : ideProject.resourcePaths) {
                        jsonWriter.value(path.toString());
                    }
                    for (Path path : runConfig.classpath) {
                        jsonWriter.value(path.toString());
                    }
                    jsonWriter.endArray();
                    jsonWriter.endObject();
                }
                jsonWriter.endArray();
                jsonWriter.endObject();
                jsonWriter.flush();
            }
            atomicFile.commit();
        }
    }
}
