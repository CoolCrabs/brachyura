package io.github.coolcrabs.brachyura.project.java;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationUnitBuilder;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeProject;
import io.github.coolcrabs.brachyura.ide.IdeProject.IdeProjectBuilder;
import io.github.coolcrabs.brachyura.project.Task;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.javacompilelib.JavaCompilationUnit;

public abstract class SimpleJavaProject extends BaseJavaProject {
    /**
     * Eg: examplegame-0.1
     */
    public abstract String getJarBaseName();
    public abstract int getJavaVersion();

    @Override
    public void getTasks(Consumer<Task> p) {
        super.getTasks(p);
        p.accept(Task.of("build", this::build));
    }

    @Override
    public IdeProject getIdeProject() {
        return new IdeProjectBuilder()
            .sourcePaths(getSrcDir())
            .resourcePaths(getResourcesDir())
            .dependencies(dependencies.get())
            .build();
    }

    public boolean build() {
        JavaCompilationUnit javaCompilationUnit = new JavaCompilationUnitBuilder()
            .sourceDir(getSrcDir())
            .outputDir(getBuildClassesDir())
            .classpath(getCompileDependencies())
            .options(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, getJavaVersion()))
            .build();
        if (!compile(javaCompilationUnit)) return false;
        try {
            Path outjar = getBuildLibsDir().resolve(getJarBaseName() + ".jar");
            Path outjarsources = getBuildLibsDir().resolve(getJarBaseName() + "-sources.jar");
            Files.deleteIfExists(outjar);
            Files.deleteIfExists(outjarsources);
            try (
                AtomicFile aoutjar = new AtomicFile(outjar);
                AtomicFile aoutjarsources = new AtomicFile(outjarsources);
            ) {
                Files.deleteIfExists(aoutjar.tempPath);
                Files.deleteIfExists(aoutjarsources.tempPath);
                try (
                    FileSystem foutjar = FileSystemUtil.newJarFileSystem(aoutjar.tempPath);
                    FileSystem foutjarsources = FileSystemUtil.newJarFileSystem(aoutjarsources.tempPath);
                ) {
                    PathUtil.copyDir(getBuildClassesDir(), foutjar.getPath("/"));
                    PathUtil.copyDir(getBuildResourcesDir(), foutjar.getPath("/"));
                    PathUtil.copyDir(getSrcDir(), foutjarsources.getPath("/"));
                }
                aoutjar.commit();
                aoutjarsources.commit();
            }
            
            return true;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public final Lazy<List<JavaJarDependency>> dependencies = new Lazy<>(this::getDependencies);
    public List<JavaJarDependency> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public List<Path> getCompileDependencies() {
        List<JavaJarDependency> deps = dependencies.get();
        ArrayList<Path> result = new ArrayList<>(deps.size());
        for (int i = 0; i < deps.size(); i++) {
            result.set(i, deps.get(i).jar);
        }
        return result;
    }
}
