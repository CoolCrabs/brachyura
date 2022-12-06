package io.github.coolcrabs.brachyura.recombobulator.remapper;

import java.io.InputStream;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.recombobulator.ClassInfo;

public interface RemapperOutputConsumer {
    void outputClass(String path, ClassInfo ci, Object tag);
    void outputFile(String path, Supplier<InputStream> isSup, Object tag);
}
