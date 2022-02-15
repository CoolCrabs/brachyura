package io.github.coolcrabs.brachyura.project;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.ide.IdeModule;
import io.github.coolcrabs.brachyura.ide.IdeModule.RunConfigBuilder;
import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.project.java.BaseJavaProject;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import io.github.coolcrabs.brachyura.util.Util;

class BuildscriptProject extends BaseJavaProject {
    @Override
    public Path getProjectDir() {
        return super.getProjectDir().resolve("buildscript");
    }

    @Override
    public void getRunConfigTasks(Consumer<Task> p) {
        //noop
    }

    @Override
    public IdeModule[] getIdeModules() {
        Tasks t = new Tasks();
        Optional<Project> o = project.get();
        if (o.isPresent()) o.get().getTasks(t);
        ArrayList<RunConfigBuilder> runConfigs = new ArrayList<>(t.t.size());
        Path cwd = getProjectDir().resolve("run");
        PathUtil.createDirectories(cwd);
        for (Map.Entry<String, Task> e : t.t.entrySet()) {
            runConfigs.add(
                new RunConfigBuilder()
                    .name(e.getKey())
                    .cwd(cwd)
                    .mainClass("io.github.coolcrabs.brachyura.project.BuildscriptDevEntry")
                    .classpath(getCompileDependencies())
                    .args(
                        () -> Arrays.asList(
                            super.getProjectDir().toString(),
                            getCompileDependencies().stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator)),
                            e.getKey()
                        )
                    )
            );
        }
        return new IdeModule[] {
            new IdeModule.IdeModuleBuilder()
                .name("Buildscript")
                .root(getProjectDir())
                .sourcePath(getSrcDir())
                .dependencies(this::getIdeDependencies)
                .runConfigs(runConfigs)
            .build()
        };
    }

    public final Lazy<Optional<Project>> project = new Lazy<>(this::createProject);
    @SuppressWarnings("all")
    public Optional<Project> createProject() {
        try {
            ClassLoader b = getBuildscriptClassLoader();
            if (b == null) return Optional.empty();
            Class projectclass = Class.forName("Buildscript", true, b);
            if (Project.class.isAssignableFrom(projectclass)) {
                return Optional.of((Project) projectclass.getDeclaredConstructor().newInstance());
            } else {
                Logger.warn("Buildscript must be instance of Project");
                return Optional.empty();
            }
        } catch (Exception e) {
            Logger.warn("Error getting project:");
            Logger.warn(e);
            return Optional.empty();
        }
    }

    public ClassLoader getBuildscriptClassLoader() {
        JavaCompilationResult compilation = new JavaCompilation()
            .addSourceDir(getSrcDir())
            .addClasspath(getCompileDependencies())
            .addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, 8))
            .compile();
        if (compilation == null) {
            Logger.warn("Buildscript compilation failed!");
            return null;
        } else {
            BuildscriptClassloader r = new BuildscriptClassloader(BuildscriptProject.class.getClassLoader());
            compilation.getInputs(r);
            return r;
        }
    }

    public List<JavaJarDependency> getIdeDependencies() {
        List<Path> compileDeps = getCompileDependencies();
        ArrayList<JavaJarDependency> result = new ArrayList<>(compileDeps.size());
        for (Path p : compileDeps) {
            Path source = p.getParent().resolve(p.getFileName().toString().replace(".jar", "-sources.jar"));
            if (!Files.exists(source)) source = null;
            result.add(new JavaJarDependency(p, source, null));
        }
        return result;
    }

    @Override
    public List<Path> getCompileDependencies() {
        return EntryGlobals.buildscriptClasspath;
    }

    static class BuildscriptClassloader extends ClassLoader implements ProcessingSink {
        HashMap<String, byte[]> classes = new HashMap<>();

        BuildscriptClassloader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public void sink(Supplier<InputStream> in, ProcessingId id) {
            try {
                if (id.path.endsWith(".class")) {
                    try (InputStream i = in.get()) {
                        classes.put(id.path.substring(0, id.path.length() - 6).replace("/", "."), StreamUtil.readFullyAsBytes(i));
                    }
                }
            } catch (Exception e) {
                throw Util.sneak(e);
            }
        }
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] data = classes.get(name);
            if (data == null) return super.findClass(name);
            return defineClass(name, data, 0, data.length);
        }
    }
}
