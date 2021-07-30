package io.github.coolcrabs.brachyura.decompiler;

import java.nio.file.Path;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.mappingio.tree.MappingTree;

public interface BrachyuraDecompiler {
    boolean lineNumberMappingsSupport();
    String getName();
    String getVersion();
    int getThreadCount();
    void decompile(Path jar, List<Path> classpath, @Nullable Path outputJar, @Nullable Path outputLineNumberMappings, @Nullable MappingTree tree, int namespace);
}
