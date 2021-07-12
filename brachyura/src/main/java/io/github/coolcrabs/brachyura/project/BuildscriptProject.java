package io.github.coolcrabs.brachyura.project;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationUnitBuilder;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.exception.TaskFailedException;
import io.github.coolcrabs.brachyura.ide.Vscode;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.javacompilelib.JavaCompilation;
import io.github.coolcrabs.javacompilelib.JavaCompilationUnit;
import io.github.coolcrabs.javacompilelib.LocalJavaCompilation;

class BuildscriptProject extends BaseJavaProject {
    @Override
    public Path getProjectDir() {
        return super.getProjectDir().resolve("buildscript");
    }

    @Override
    public void getTasks(Consumer<Task> p) {
        p.accept(Task.of("vscode", this::vscode));
    }

    public void vscode() {
        Path vscode = getProjectDir().resolve(".vscode");
        Vscode.updateSettingsJson(vscode.resolve("settings.json"), getIdeDependencies());
        Vscode.LaunchJson launchJson = new Vscode.LaunchJson();
        Tasks t = new Tasks();
        project.get().getTasks(t);
        Vscode.LaunchJson.Configuration[] configs = new Vscode.LaunchJson.Configuration[t.t.size()];
        int i = 0;
        for (Map.Entry<String, Task> e : t.t.entrySet()) {
            Vscode.LaunchJson.Configuration run = new Vscode.LaunchJson.Configuration();
            run.name = e.getKey();
            run.cwd = "${workspaceFolder}/run";
            run.mainClass = "io.github.coolcrabs.brachyura.project.BuildscriptDevEntry";
            run.args = new String[] {super.getProjectDir().toAbsolutePath().toString(), e.getKey()};
            configs[i] = run;
            ++i;
        }
        
        launchJson.configurations = configs;
        Vscode.updateLaunchJson(vscode.resolve("launch.json"), launchJson);
        PathUtil.resolveAndCreateDir(getProjectDir(), "run");
    }

    public final Lazy<Project> project = new Lazy<>(this::createProject);
    @SuppressWarnings("all")
    public Project createProject() {
        try {
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] {getBuildscriptClaspath().toUri().toURL()}, BuildscriptProject.class.getClassLoader());
            Class projectclass = Class.forName("Buildscript", true, classLoader);
            if (Project.class.isAssignableFrom(projectclass)) {
                return (Project) projectclass.getDeclaredConstructor().newInstance();
            } else {
                throw new TaskFailedException("Buildscript must be instance of Project");
            }
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    public Path getBuildscriptClaspath() {
        JavaCompilationUnit javaCompilationUnit = new JavaCompilationUnitBuilder()
            .sourceDir(getSrcDir())
            .outputDir(getBuildClassesDir())
            .classpath(getCompileDependencies())
            .options(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, 8))
            .build();
        if (!compile(javaCompilationUnit)) {
            throw new TaskFailedException("Buildscript compilation failed!");
        }
        return getBuildClassesDir();
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

    @Override
    public JavaCompilation getCompiler() {
        return LocalJavaCompilation.INSTANCE;
    }

    @Override
    public boolean processResources(Path source, Path target) throws IOException {
        return true; // It's a buildscript no resources for u
    }
}
