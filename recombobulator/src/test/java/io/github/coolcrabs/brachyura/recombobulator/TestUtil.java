package io.github.coolcrabs.brachyura.recombobulator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtil {
    public static final Path ROOT;
    
    static {
        Path p = Paths.get("").toAbsolutePath();
        while (!p.getFileName().toString().equals("brachyura") && !Files.exists(p.resolve(".brachyuradirmarker"))) {
            p = p.getParent();
        }
        ROOT = p;
    }

    public static final Path TMP = ROOT.resolve("recombobulator").resolve("tmp");
}
