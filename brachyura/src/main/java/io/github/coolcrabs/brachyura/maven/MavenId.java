package io.github.coolcrabs.brachyura.maven;

import java.security.InvalidParameterException;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

public final class MavenId {
    public final String groupId;
    public final String artifactId;
    public final String version;
    public final @Nullable String classifier;

    public MavenId(String maven) {
        String[] a = maven.split(":");
        if (a.length < 3 || a.length > 4) throw new InvalidParameterException("Bad maven id " + maven);
        this.groupId = a[0];
        this.artifactId = a[1];
        this.version = a[2];
        this.classifier = a.length == 4 ? a[3] : null;
    }

    public MavenId(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null);
    }

    public MavenId(String groupId, String artifactId, String version, String classifier) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version, classifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof MavenId) {
            MavenId mavenId = (MavenId)obj;
            return this.groupId.equals(mavenId.groupId) && this.artifactId.equals(mavenId.artifactId) && this.version.equals(mavenId.version) && Objects.equals(this.classifier, mavenId.classifier);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        if (classifier != null) {
            return groupId + ":" + artifactId + ":" + version + ":" + classifier;
        }
        return groupId + ":" + artifactId + ":" + version;
    }
}
