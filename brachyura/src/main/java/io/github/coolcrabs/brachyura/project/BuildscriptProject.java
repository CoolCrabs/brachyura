package io.github.coolcrabs.brachyura.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.compiler.java.CompilationFailedException;
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

    public final Lazy<Properties> properties = new Lazy<>(this::createProperties);

    Properties createProperties() {
        try {
            Path file = getProjectDir().resolve("buildscript.properties");
            Properties properties0 = new Properties();
            if (Files.exists(file)) {
                try (BufferedReader r = Files.newBufferedReader(file)) {
                    properties0.load(r);
                }
            } else {
                Logger.info("Didn't find buildscript.properties; autogenerating it.");
                properties0.setProperty("name", super.getProjectDir().getFileName().toString());
                properties0.setProperty("javaVersion", "8");
                try (BufferedWriter w = Files.newBufferedWriter(file)) {
                    properties0.store(w, "Brachyura Buildscript Properties");
                }
            }
            return properties0;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String getPropOrThrow(String property) {
        String r = properties.get().getProperty(property);
        if (r == null) throw new RuntimeException("Missing property " + property + " in buildscript.properties");
        return r;
    }

    @Override
    public IdeModule[] getIdeModules() {
        Tasks t = new Tasks();
        Optional<Project> o = project.get();
        if (o.isPresent()) o.get().getTasks(t);
        ArrayList<RunConfigBuilder> runConfigs = new ArrayList<>(t.t.size());
        Path cwd = getProjectDir().resolve("run");
        PathUtil.createDirectories(cwd);
        int javaVersion = Integer.parseInt(getPropOrThrow("javaVersion"));
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
                .name("BScript-" + getPropOrThrow("name"))
                .root(getProjectDir())
                .sourcePath(getSrcDir())
                .dependencies(this::getIdeDependencies)
                .runConfigs(runConfigs)
                .javaVersion(javaVersion)
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
        int javaVersion = Integer.parseInt(getPropOrThrow("javaVersion"));
        try {
            JavaCompilationResult compilation = new JavaCompilation()
                .addSourceDir(getSrcDir())
                .addClasspath(getCompileDependencies())
                .addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, javaVersion))
                .compile();
            BuildscriptClassloader r = new BuildscriptClassloader(BuildscriptProject.class.getClassLoader());
            compilation.getInputs(r);
            return r;
        } catch (CompilationFailedException e) {
            Logger.warn("Buildscript compilation failed!");
            return null;
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
