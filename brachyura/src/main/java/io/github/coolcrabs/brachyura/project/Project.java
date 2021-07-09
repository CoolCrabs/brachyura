package io.github.coolcrabs.brachyura.project;

import java.nio.file.Path;

import io.github.coolcrabs.brachyura.util.PathUtil;

public abstract class Project {
    public abstract Path getProjectDir();

    public Path getLocalBrachyuraPath() {
        return PathUtil.resolveAndCreateDir(getProjectDir(), ".brachyura");
    }
}
