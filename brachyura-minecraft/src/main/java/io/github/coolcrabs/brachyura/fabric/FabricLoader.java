package io.github.coolcrabs.brachyura.fabric;

import java.io.Reader;
import java.nio.file.Files;

import com.google.gson.Gson;

import io.github.coolcrabs.brachyura.dependency.FileDependency;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.util.Util;

public class FabricLoader {
    public final JavaJarDependency jar;
    public final JavaJarDependency[] clientDeps;
    public final JavaJarDependency[] commonDeps;
    public final JavaJarDependency[] serverDeps;

    public FabricLoader(String mavenRepo, MavenId id) {
        try {
            this.jar = Maven.getMavenJarDep(mavenRepo, id);
            FileDependency jsonFile = Maven.getMavenFileDep(mavenRepo, id, ".json");
            FloaderMeta floaderMeta;
            try (Reader jsonReader = Files.newBufferedReader(jsonFile.file)) {
                floaderMeta = new Gson().fromJson(jsonReader, FloaderMeta.class);
            }
            clientDeps = new JavaJarDependency[floaderMeta.libraries.client.length];
            for (int i = 0; i < floaderMeta.libraries.client.length; i++) {
                FloaderMeta.Dep dep = floaderMeta.libraries.client[i];
                clientDeps[i] = Maven.getMavenJarDep(dep.url, new MavenId(dep.name));
            }
            commonDeps = new JavaJarDependency[floaderMeta.libraries.common.length];
            for (int i = 0; i < floaderMeta.libraries.common.length; i++) {
                FloaderMeta.Dep dep = floaderMeta.libraries.common[i];
                commonDeps[i] = Maven.getMavenJarDep(dep.url, new MavenId(dep.name));
            }
            serverDeps = new JavaJarDependency[floaderMeta.libraries.server.length];
            for (int i = 0; i < floaderMeta.libraries.server.length; i++) {
                FloaderMeta.Dep dep = floaderMeta.libraries.server[i];
                serverDeps[i] = Maven.getMavenJarDep(dep.url, new MavenId(dep.name));
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    class FloaderMeta {
        Libraries libraries;

        class Libraries {
            Dep[] client;
            Dep[] common;
            Dep[] server;
        }

        class Dep {
            String name;
            String url;
        }
    }
}
