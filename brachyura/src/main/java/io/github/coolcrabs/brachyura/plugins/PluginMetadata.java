package io.github.coolcrabs.brachyura.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.util.Util;

public class PluginMetadata {
    public static final String V1_HEADER = "Brachyura Plugin Metadata v1";

    public final MavenId mavenId;
    public final List<PluginMetadataDependency> dependenices = new ArrayList<>();

    public PluginMetadata(MavenId mavenId) {
        this.mavenId = mavenId;
    }

    public static PluginMetadata read(BufferedReader r) {
        try {
            String header = r.readLine();
            
            PluginMetadata result = new PluginMetadata(new MavenId(r.readLine()));
            String depId;
            while ((depId = r.readLine()) != null) {
                String url = Objects.requireNonNull(r.readLine());
                String sourcesUrl = Objects.requireNonNull(r.readLine());
                if (sourcesUrl.isEmpty()) sourcesUrl = null;
                result.addDep(new MavenId(depId), url, sourcesUrl);
            }
            return result;
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public void addDep(MavenId id, String url, @Nullable String sourcesUrl) {
        dependenices.add(new PluginMetadataDependency(id, url, sourcesUrl));
    }

    public void addMaven(String mavenRepo, MavenId mavenId) {
        addMaven(mavenRepo, mavenId, true);
    }

    public void addMaven(String mavenRepo, MavenId mavenId, boolean sources) {
        String a = mavenRepo + mavenId.groupId.replace('.', '/') + "/" + mavenId.artifactId + "/" + mavenId.version + "/" + mavenId.artifactId + "-" + mavenId.version;
        addDep(mavenId, a + ".jar", sources ? a + "-sources.jar" : null);
    }

    public static class PluginMetadataDependency {
        public final MavenId id;
        public final String url;
        public final @Nullable String sourcesUrl;

        public PluginMetadataDependency(MavenId id, String url, @Nullable String sourcesUrl) {
            this.id = id;
            this.url = url;
            this.sourcesUrl = sourcesUrl;
        }
    }
}
