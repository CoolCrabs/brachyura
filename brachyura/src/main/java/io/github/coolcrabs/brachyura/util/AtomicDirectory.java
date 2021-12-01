package io.github.coolcrabs.brachyura.util;

import java.nio.file.Path;

public class AtomicDirectory implements AutoCloseable {
    boolean commited = false;
    final Path target;
    public final Path tempPath;

    public AtomicDirectory(Path target) {
        this.target = target;
        this.tempPath = PathUtil.tempDir(target);
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
                PathUtil.deleteDirectory(tempPath);
            } catch (Exception e) {
                // No need to care
            }
        }
    }
}
