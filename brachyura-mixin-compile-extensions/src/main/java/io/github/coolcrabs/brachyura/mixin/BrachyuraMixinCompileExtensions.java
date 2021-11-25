package io.github.coolcrabs.brachyura.mixin;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BrachyuraMixinCompileExtensions {
    private BrachyuraMixinCompileExtensions() { }

    static final Path location;

    static {
        try {
            location = Paths.get(BrachyuraMixinCompileExtensions.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getJar() {
        return location;
    }
}
