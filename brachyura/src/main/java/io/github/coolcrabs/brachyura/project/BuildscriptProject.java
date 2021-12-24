package io.github.coolcrabs.brachyura.project;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.exception.TaskFailedException;
import io.github.coolcrabs.brachyura.ide.IdeProject;
import io.github.coolcrabs.brachyura.ide.IdeProject.IdeProjectBuilder;
import io.github.coolcrabs.brachyura.ide.IdeProject.RunConfig;
import io.github.coolcrabs.brachyura.ide.IdeProject.RunConfig.RunConfigBuilder;
import io.github.coolcrabs.brachyura.processing.sinks.DirectoryProcessingSink;
import io.github.coolcrabs.brachyura.project.java.BaseJavaProject;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

class BuildscriptProject extends BaseJavaProject {
    @Override
    public Path getProjectDir() {
        return super.getProjectDir().resolve("buildscript");
    }

    @Override
    public IdeProject getIdeProject() {
        Tasks t = new Tasks();
        Optional<Project> o = project.get();
        if (o.isPresent()) o.get().getTasks(t);
        ArrayList<RunConfig> runConfigs = new ArrayList<>(t.t.size());
        Path cwd = getProjectDir().resolve("run");
        PathUtil.createDirectories(cwd);
        for (Map.Entry<String, Task> e : t.t.entrySet()) {
            runConfigs.add(
                new RunConfigBuilder()
                    .name(e.getKey())
                    .cwd(cwd)
                    .mainClass("io.github.coolcrabs.brachyura.project.BuildscriptDevEntry")
                    .classpath(getCompileDependencies())
                    .args(super.getProjectDir().toString(), e.getKey())
                .build()
            );
        }
        return new IdeProjectBuilder()
            .name("Buildscript")
            .sourcePaths(getSrcDir())
            .dependencies(getIdeDependencies())
            .runConfigs(runConfigs)
        .build();
    }

    public final Lazy<Optional<Project>> project = new Lazy<>(this::createProject);
    @SuppressWarnings("all")
    public Optional<Project> createProject() {
        try {
            Path b = getBuildscriptClaspath();
            if (b == null) return Optional.empty();
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] {getBuildscriptClaspath().toUri().toURL()}, BuildscriptProject.class.getClassLoader());
            Class projectclass = Class.forName("Buildscript", true, classLoader);
            if (Project.class.isAssignableFrom(projectclass)) {
                return Optional.of((Project) projectclass.getDeclaredConstructor().newInstance());
            } else {
                throw new TaskFailedException("Buildscript must be instance of Project");
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public @Nullable Path getBuildscriptClaspath() {
        Path buildClassesDir = getBuildClassesDir();
        PathUtil.deleteDirectoryChildren(buildClassesDir);
        JavaCompilationResult compilation = new JavaCompilation()
            .addSourceDir(getSrcDir())
            .addClasspath(getCompileDependencies())
            .addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, 8))
            .compile();
        if (compilation == null) {
            Logger.warn("Buildscript compilation failed!");
            return null;
        } else {
            compilation.getInputs(new DirectoryProcessingSink(buildClassesDir)); // TODO replace with custom classloader
        }
        return buildClassesDir;
    }

    public List<JavaJarDependency> getIdeDependencies() {
        List<Path> compileDeps = getCompileDependencies();
        ArrayList<JavaJarDependency> result = new ArrayList<>(compileDeps.size());
        for (Path p : compileDeps) {
            result.add(new JavaJarDependency(p, null, null));
        }
        return result;
    }

    @Override
    public List<Path> getCompileDependencies() {
        return BrachyuraEntry.classpath;
    }

    public Path getBuildClassesDir() {
        return PathUtil.resolveAndCreateDir(getBuildDir(), "classes");
    }
}
