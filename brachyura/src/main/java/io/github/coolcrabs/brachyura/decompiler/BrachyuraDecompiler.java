package io.github.coolcrabs.brachyura.decompiler;

import java.nio.file.Path;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.mappingio.tree.MappingTree;

public interface BrachyuraDecompiler {
    LineNumberMappingsSupport lineNumberMappingsSupport();
    String getName();
    String getVersion();
    void decompile(Path jar, List<Path> classpath, @Nullable Path outputJar, @Nullable Path outputLineNumberMappings, @Nullable MappingTree tree, int namespace);

    enum LineNumberMappingsSupport {
        NONE,
        REMAP_EXISTING, // fabric flower
        REPLACE // cfr
    }
}
