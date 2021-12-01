package io.github.coolcrabs.brachyura.project.java;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.ide.Ide;
import io.github.coolcrabs.brachyura.ide.IdeProject;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.project.Project;
import io.github.coolcrabs.brachyura.project.Task;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.javacompilelib.JavaCompilation;
import io.github.coolcrabs.javacompilelib.LocalJavaCompilation;

public abstract class BaseJavaProject extends Project {
    public abstract IdeProject getIdeProject();

    public int getJavaVersion() {
        return 8;
    }

    @Override
    public void getTasks(Consumer<Task> p) {
        super.getTasks(p);
        getIdeTasks(p);
    }

    public void getIdeTasks(Consumer<Task> p) {
        for (Ide ide : Ide.getIdes()) {
            p.accept(Task.of(ide.ideName(), () -> ide.updateProject(getProjectDir(), getIdeProject())));
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
    
    public JavaCompilation getCompiler() {
        return LocalJavaCompilation.INSTANCE;
    }
}
