package io.github.coolcrabs.brachyura.recombobulator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Obtainor {
    static String mcurl = "https://piston-data.mojang.com/v1/objects/c0898ec7c6a5a2eaa317770203a1554260699994/client.jar";
    static String inturl = "https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/1.19.tiny";

    public static Path getMc() {
        return get(mcurl, "client1_19.jar");
    }

    public static Path getInt() {
        return get(inturl, "int.tiny");
    }

    static Path get(String url, String name) {
        Path tmpPath = TestUtil.TMP;
        try {
            Files.createDirectories(tmpPath);
        } catch (IOException e1) {
            throw new UncheckedIOException(e1);
        } 
        Path p = tmpPath.resolve(name);
        if (!Files.exists(p)) {
            try {
                try (InputStream is = new URL(url).openStream()) {
                    Path tmp = tmpPath.resolve(System.currentTimeMillis() + ".tmp");
                    Files.copy(is, tmp);
                    Files.move(tmp, p, StandardCopyOption.ATOMIC_MOVE);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return p;
    }
}
