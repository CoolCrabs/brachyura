package io.github.coolcrabs.brachyura.fabric;

import io.github.coolcrabs.brachyura.maven.MavenId;

public class FabricMaven {
    private FabricMaven() { }

    public static final String URL = "https://maven.fabricmc.net/";
    public static final String GROUP_ID = "net.fabricmc";

    public static MavenId intermediary(String version) {
        return new MavenId(GROUP_ID, "intermediary", version);
    }

    public static MavenId yarn(String version) {
        return new MavenId(GROUP_ID, "yarn", version);
    }

    public static MavenId loader(String version) {
        return new MavenId(GROUP_ID, "fabric-loader", version);
    }

    public static MavenId devLaunchInjector(String version) {
        return new MavenId(GROUP_ID, "dev-launch-injector", version);
    }
}
