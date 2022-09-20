package io.github.coolcrabs.brachyura.project.java;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeModule;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;

public abstract class SimpleJavaModule extends BuildModule {
    public abstract Path[] getSrcDirs();
    public abstract Path[] getResourceDirs();

    public final Lazy<List<JavaJarDependency>> dependencies = new Lazy<>(this::createDependencies);
    protected abstract List<JavaJarDependency> createDependencies();

    protected List<BuildModule> getModuleDependencies() {
        return Collections.emptyList();
    }

    public List<Path> getCompileDependencies() {
        List<JavaJarDependency> deps = dependencies.get();
        ArrayList<Path> result = new ArrayList<>(deps.size());
        for (int i = 0; i < deps.size(); i++) {
            result.add(deps.get(i).jar);
        }
        return result;
    }

    @Override
    protected ProcessingSource createCompilationOutput() {
        return compilationResult.get();
    }

    public final Lazy<JavaCompilationResult> compilationResult = new Lazy<>(() -> createCompilation().compile());

    protected JavaCompilation createCompilation() {
        JavaCompilation r = new JavaCompilation()
            .addSourceDir(getSrcDirs())
            .addClasspath(getCompileDependencies())
            .addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, getJavaVersion()));
        for (BuildModule m : getModuleDependencies()) {
            r.addClasspath(m.compilationOutput.get());
        }
        return r;
    }

    @Override
    public IdeModule createIdeModule() {
        return new IdeModule.IdeModuleBuilder()
            .name(getModuleName())
            .root(getModuleRoot())
            .javaVersion(getJavaVersion())
            .sourcePaths(getSrcDirs())
            .resourcePaths(getResourceDirs())
            .dependencies(dependencies.get())
            .dependencyModules(getModuleDependencies().stream().map(m -> m.ideModule.get()).collect(Collectors.toList()))
            .build();
    }
}
