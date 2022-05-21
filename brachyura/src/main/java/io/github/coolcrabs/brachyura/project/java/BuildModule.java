package io.github.coolcrabs.brachyura.project.java;

import java.nio.file.Path;

import io.github.coolcrabs.brachyura.ide.IdeModule;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.util.Lazy;

/**
 * Represents sources that are compiled together
 */
public abstract class BuildModule {
    public int getJavaVersion() {
        return 8;
    }

    public abstract String getModuleName();
    public abstract Path getModuleRoot();

    public final Lazy<ProcessingSource> compilationOutput = new Lazy<>(this::createCompilationOutput);
    protected abstract ProcessingSource createCompilationOutput();

    public abstract IdeModule ideModule();
}
