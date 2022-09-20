package io.github.coolcrabs.brachyura.compiler.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.util.LoggerWriter;
import io.github.coolcrabs.brachyura.util.Util;

public class JavaCompilation {
    private ArrayList<String> options = new ArrayList<>();
    private ArrayList<Path> sourceFiles = new ArrayList<>();
    private ArrayList<Path> sourcePath = new ArrayList<>();
    private ArrayList<Path> classpath = new ArrayList<>();
    private ArrayList<ProcessingSource> classpathSources = new ArrayList<>();
    private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public JavaCompilation addOption(String... options) {
        Collections.addAll(this.options, options);
        return this;
    }

    public JavaCompilation addSourceFile(Path path) {
        this.sourceFiles.add(path);
        return this;
    }

    public JavaCompilation addSourceDir(Path... paths) {
        for (Path p : paths) addSourceDir(p);
        return this;
    }

    public JavaCompilation addSourceDir(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".java")) {
                        sourceFiles.add(file);
                    } else {
                        Logger.warn("Unrecognized file type for file {} in java src dir", file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw Util.sneak(e);
        }
        return this;
    }

    public JavaCompilation addSourcePathFile(Path path) {
        this.sourcePath.add(path);
        return this;
    }

    public JavaCompilation addSourcePathDir(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().endsWith(".java")) {
                        sourcePath.add(file);
                    } else {
                        Logger.warn("Unrecognized file type for file {} in java src path dir", file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw Util.sneak(e);
        }
        return this;
    }

    public JavaCompilation addClasspath(List<Path> paths) {
        classpath.addAll(paths);
        return this;
    }

    public JavaCompilation addClasspath(Path... paths) {
        Collections.addAll(classpath, paths);
        return this;
    }

    /**
     * Should mostly be used for other compilation outputs; jars and directories should use the other methods
     */
    public JavaCompilation addClasspath(ProcessingSource source) {
        classpathSources.add(source);
        return this;
    }

    public JavaCompilation setCompiler(JavaCompiler compiler) {
        this.compiler = compiler;
        return this;
    }

    ArrayList<File> bruh(ArrayList<Path> p) {
        ArrayList<File> r = new ArrayList<>(p.size());
        for (int i = 0; i < p.size(); i++) {
            r.add(p.get(i).toFile());
        }
        return r;
    }

    public JavaCompilationResult compile() throws CompilationFailedException {
        try {
            try (BrachyuraJavaFileManager fileManager = new BrachyuraJavaFileManager()) {
                boolean success;
                fileManager.setLocation(StandardLocation.CLASS_PATH, bruh(classpath));
                fileManager.setLocation(StandardLocation.SOURCE_PATH, bruh(sourcePath));
                for (ProcessingSource s : classpathSources) {
                    fileManager.extraCp.add(s);
                }
                try (LoggerWriter w = new LoggerWriter()) {
                    CompilationTask compilationTask = compiler.getTask(w, fileManager, BrachyuraDiagnosticListener.INSTANCE, options, null, fileManager.getJavaFileObjectsFromFiles(bruh(sourceFiles)));
                    success = compilationTask.call();
                }
                if (success) {
                    return new JavaCompilationResult(fileManager);
                }
                throw new CompilationFailedException();
            }
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
}
