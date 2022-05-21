package io.github.coolcrabs.brachyura.quilt;

import io.github.coolcrabs.brachyura.maven.MavenId;

public class QuiltMaven {
    private QuiltMaven() { }

    public static final String URL = "https://maven.quiltmc.org/repository/release/";
    public static final String GROUP_ID = "org.quiltmc";

    public static MavenId loader(String version) {
        return new MavenId(GROUP_ID, "quilt-loader", version);
    }

    public static MavenId quiltMappings(String version) {
        return new MavenId(GROUP_ID, "quilt-mappings", version);
    }
}
