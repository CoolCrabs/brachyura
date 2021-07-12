package io.github.coolcrabs.brachyura.project;

import java.nio.file.Path;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.util.PathUtil;

public class Project {
    public void getTasks(Consumer<Task> p) {
        // no default tasks
    }

    public Path getProjectDir() {
        return EntryGlobals.projectDir;
    }

    public Path getLocalBrachyuraPath() {
        return PathUtil.resolveAndCreateDir(getProjectDir(), ".brachyura");
    }
}
