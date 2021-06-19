package io.github.coolcrabs.brachyura.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PathUtil {
    private PathUtil() { }

    public static final Path HOME = Path.of(System.getProperty("user.home"));

    public static Path brachyuraPath() {
        return HOME.resolve(".brachyura");
    }

    public static Path cachePath() {
        return brachyuraPath().resolve("cache");
    }

    public static InputStream inputStream(Path path) {
        try {
            Files.createDirectories(path.getParent());
            return new BufferedInputStream(Files.newInputStream(path));
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    /**
     * Returns a temp file in the same directory as the target file
     */
    public static Path tempFile(Path target) {
        try {
            Files.createDirectories(target.getParent());
            return Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static void moveAtoB(Path a, Path b) {
        try {
            Files.move(a, b, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.delete(b);
            } catch (Exception e2) {
                // File prob wasn't created
            }
            throw Util.sneak(e);
        }
    }
}
