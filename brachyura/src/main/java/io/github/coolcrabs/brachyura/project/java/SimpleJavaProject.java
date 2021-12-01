package io.github.coolcrabs.brachyura.project.java;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeProject;
import io.github.coolcrabs.brachyura.ide.IdeProject.IdeProjectBuilder;
import io.github.coolcrabs.brachyura.processing.sinks.AtomicZipProcessingSink;
import io.github.coolcrabs.brachyura.processing.sources.DirectoryProcessingSource;
import io.github.coolcrabs.brachyura.processing.sources.ProcessingSponge;
import io.github.coolcrabs.brachyura.project.Task;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;

public abstract class SimpleJavaProject extends BaseJavaProject {
    /**
     * Eg: examplegame-0.1
     */
    public abstract String getJarBaseName();

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
        JavaCompilation compilation = new JavaCompilation()
            .addSourceDir(getSrcDir())
            .addClasspath(getCompileDependencies())
            .addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, getJavaVersion()));
        ProcessingSponge classes = new ProcessingSponge();
        if (!compilation.compile(classes)) return false;
        Path outjar = getBuildLibsDir().resolve(getJarBaseName() + ".jar");
        Path outjarsources = getBuildLibsDir().resolve(getJarBaseName() + "-sources.jar");
        try (
            AtomicZipProcessingSink jarSink = new AtomicZipProcessingSink(outjar);
            AtomicZipProcessingSink jarSourcesSink = new AtomicZipProcessingSink(outjarsources);
        ) {
            resourcesProcessingChain().apply(jarSink, new DirectoryProcessingSource(getResourcesDir()));
            classes.getInputs(jarSink);
            new DirectoryProcessingSource(getSrcDir()).getInputs(jarSourcesSink);
            jarSink.commit();
            jarSourcesSink.commit();
        }
        return true;
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
