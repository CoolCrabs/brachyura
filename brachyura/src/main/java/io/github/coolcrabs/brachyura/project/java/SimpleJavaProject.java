package io.github.coolcrabs.brachyura.project.java;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeModule;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.maven.MavenPublishing;
import io.github.coolcrabs.brachyura.processing.sinks.AtomicZipProcessingSink;
import io.github.coolcrabs.brachyura.processing.sources.DirectoryProcessingSource;
import io.github.coolcrabs.brachyura.processing.sources.ProcessingSponge;
import io.github.coolcrabs.brachyura.project.Task;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import java.util.function.Supplier;

public abstract class SimpleJavaProject extends BaseJavaProject {
    public abstract MavenId getId();
    
    public String getJarBaseName() {
        return getId().artifactId + "-" + getId().version;
    }

    @Override
    public void getTasks(Consumer<Task> p) {
        super.getTasks(p);
        p.accept(Task.of("build", this::build));
        getPublishTasks(p);
    }
    
    public void getPublishTasks(Consumer<Task> p) {
        createPublishTasks(p, this::build);
    }
    
    public static void createPublishTasks(Consumer<Task> p, Supplier<JavaJarDependency> build) {
        p.accept(Task.of("publish", () -> MavenPublishing.publish(MavenPublishing.AuthenticatedMaven.ofEnv(), build.get())));
        p.accept(Task.of("publishToMavenLocal", () -> MavenPublishing.publish(MavenPublishing.AuthenticatedMaven.ofMavenLocal(), build.get())));
    }

    @Override
    public IdeModule[] getIdeModules() {
        return new IdeModule[] {new IdeModule.IdeModuleBuilder()
            .name(getId().artifactId)
            .root(getProjectDir())
            .javaVersion(getJavaVersion())
            .sourcePath(getSrcDir())
            .resourcePaths(getResourcesDir())
            .dependencies(dependencies.get())
            .build()
        };
    }

    public JavaJarDependency build() {
        JavaCompilationResult compilation = new JavaCompilation()
            .addSourceDir(getSrcDir())
            .addClasspath(getCompileDependencies())
            .addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, getJavaVersion()))
            .compile();
        ProcessingSponge classes = new ProcessingSponge();
        compilation.getInputs(classes);
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
        return new JavaJarDependency(outjar, outjar, getId());
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
