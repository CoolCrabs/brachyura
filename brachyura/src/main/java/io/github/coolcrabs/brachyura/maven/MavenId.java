package io.github.coolcrabs.brachyura.maven;

import java.security.InvalidParameterException;
import java.util.Objects;

public final class MavenId {
    public final String groupId;
    public final String artifactId;
    public final String version;

    public MavenId(String maven) {
        String[] a = maven.split(":");
        if (a.length != 3) throw new InvalidParameterException("Bad maven id " + maven);
        this.groupId = a[0];
        this.artifactId = a[1];
        this.version = a[2];
    }

    public MavenId(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, artifactId, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof MavenId) {
            MavenId mavenId = (MavenId)obj;
            return this.groupId.equals(mavenId.groupId) && this.artifactId.equals(mavenId.artifactId) && this.version.equals(mavenId.version);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
