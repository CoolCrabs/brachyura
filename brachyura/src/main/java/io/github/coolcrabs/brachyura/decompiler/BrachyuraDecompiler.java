package io.github.coolcrabs.brachyura.decompiler;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.util.AtomicDirectory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.mappingio.tree.MappingTree;
import org.tinylog.Logger;

public abstract class BrachyuraDecompiler {
    public abstract String getName();
    public abstract String getVersion();
    public abstract int getThreadCount();
    
    public DecompileResult getDecompiled(Path jar, List<Path> classpath, Path resultDir) {
        return getDecompiled(jar, classpath, resultDir, null, 0);
    }
    
    public DecompileResult getDecompiled(Path jar, List<Path> classpath, Path resultDir, MappingTree tree, String namespace) {
        return getDecompiled(jar, classpath, resultDir, tree, tree.getNamespaceId(namespace));
    }
    
    public DecompileResult getDecompiled(Path jar, List<Path> classpath, Path resultDir, @Nullable MappingTree tree, int namespace) {
        if (!Files.exists(resultDir)) {
            Logger.info("Decompiling {} using {} {} with {} threads", jar.getFileName(), getName(), getVersion(), getThreadCount());
            long start = System.currentTimeMillis();
            try (AtomicDirectory a = new AtomicDirectory(resultDir)) {
                decompileAndLinemap(jar, classpath, a.tempPath, tree, namespace);
                a.commit();
            }
            long end = System.currentTimeMillis();
            Logger.info("Decompiled {} in {}ms", jar.getFileName(), end - start);
        }
        return getDecompileResult(jar, resultDir);
    }
    
    // Result dir is already atomic
    protected abstract DecompileResult getDecompileResult(Path jar, Path resultDir);
    
    protected abstract void decompileAndLinemap(Path jar, List<Path> classpath, Path resultDir, @Nullable MappingTree tree, int namespace);
    
    public static class DecompileResult {
        // Original jar but is linemapped in some capacity if decompiler can do so
        // Otherwise it is just the input jar
        public final Path jar;
        // Decompiled sources
        public final Path sourcesJar;
        
        public DecompileResult(Path jar, Path sourcesJar) {
            this.jar = jar;
            this.sourcesJar = sourcesJar;
        }
        
        public JavaJarDependency toJavaJarDep(@Nullable MavenId id) {
            return new JavaJarDependency(jar, sourcesJar, id);
        }
    }
}
