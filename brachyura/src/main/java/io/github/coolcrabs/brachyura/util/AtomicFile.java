package io.github.coolcrabs.brachyura.util;

import java.nio.file.Files;
import java.nio.file.Path;

import org.tinylog.Logger;

public class AtomicFile implements AutoCloseable {
    boolean commited = false;
    private final Path target;
    public final Path tempPath;

    public AtomicFile(Path target) {
        this.target = target;
        this.tempPath = PathUtil.tempFile(target);
    }

    public void commit() {
        PathUtil.moveAtoB(tempPath, target);
        commited = true;
    }

    @Override
    public void close() {
        if (!commited) {
            Logger.warn("Atomic {} not commited", target.toString());
            try {
                Files.delete(tempPath);
            } catch (Exception e) {
                // No need to care
            }
        }
    }
}
