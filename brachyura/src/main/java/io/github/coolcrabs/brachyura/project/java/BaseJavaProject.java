package io.github.coolcrabs.brachyura.project.java;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.ide.Ide;
import io.github.coolcrabs.brachyura.ide.IdeProject;
import io.github.coolcrabs.brachyura.ide.Intellijank;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.processing.sinks.DirectoryProcessingSink;
import io.github.coolcrabs.brachyura.project.Project;
import io.github.coolcrabs.brachyura.project.Task;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Objects;

public abstract class BaseJavaProject extends Project {
    public abstract IdeProject getIdeProject();

    public int getJavaVersion() {
        return 8;
    }

    @Override
    public void getTasks(Consumer<Task> p) {
        super.getTasks(p);
        getIdeTasks(p);
        getRunConfigTasks(p);
    }

    public void getIdeTasks(Consumer<Task> p) {
        for (Ide ide : Ide.getIdes()) {
            p.accept(Task.of(ide.ideName(), () -> {
                if (ide instanceof Intellijank && getBuildscriptProject() != null) {
                    ((Intellijank)ide).updateProject(getProjectDir(), getIdeProject(), getBuildscriptProject());
                } else {
                    ide.updateProject(getProjectDir(), getIdeProject());
                }
            }));
        }
    }
    
    public void getRunConfigTasks(Consumer<Task> p) {
        IdeProject ideProject = getIdeProject();
        for (IdeProject.RunConfig rc : ideProject.runConfigs) {
            p.accept(Task.of("run" + rc.name.replace(" ", ""), () -> runRunConfig(ideProject, rc)));
        }
    }
    
    public void runRunConfig(IdeProject ideProject, IdeProject.RunConfig rc) {
        try {
            JavaCompilation compilation = new JavaCompilation();
            compilation.addOption(rc.args.get().toArray(new String[0]));
            compilation.addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, getJavaVersion()));
            compilation.addOption("-proc:none");
            for (JavaJarDependency dep : ideProject.dependencies.get()) {
                compilation.addClasspath(dep.jar);
            }
            for (Path srcDir : ideProject.sourcePaths.values()) {
                compilation.addSourceDir(srcDir);
            }
            Path outDir = Files.createTempDirectory("brachyurarun");
            JavaCompilationResult result = compilation.compile();
            Objects.requireNonNull(result);
            result.getInputs(new DirectoryProcessingSink(outDir));
            ArrayList<String> command = new ArrayList<>();
            command.add(JvmUtil.CURRENT_JAVA_EXECUTABLE);
            command.addAll(rc.vmArgs.get());
            command.add("-cp");
            ArrayList<Path> cp = new ArrayList<>(rc.classpath.get());
            cp.addAll(ideProject.resourcePaths);
            cp.add(outDir);
            StringBuilder cpStr = new StringBuilder();
            for (Path p : cp) {
                cpStr.append(p.toString());
                cpStr.append(File.pathSeparator);
            }
            cpStr.setLength(cpStr.length() - 1);
            command.add(cpStr.toString());
            command.add(rc.mainClass);
            command.addAll(rc.args.get());
            new ProcessBuilder(command)
                .inheritIO()
                .directory(rc.cwd.toFile())
                .start()
                .waitFor();
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }
    
    public List<Path> getCompileDependencies() {
        return Collections.emptyList();
    }

    public ProcessorChain resourcesProcessingChain() {
        return new ProcessorChain();
    }

    public Path getBuildLibsDir() {
        return PathUtil.resolveAndCreateDir(getBuildDir(), "libs");
    }

    public Path getBuildDir() {
        return PathUtil.resolveAndCreateDir(getProjectDir(), "build");
    }

    public Path getSrcDir() {
        return getProjectDir().resolve("src").resolve("main").resolve("java");
    }

    public Path getResourcesDir() {
        return getProjectDir().resolve("src").resolve("main").resolve("resources");
    }
}
