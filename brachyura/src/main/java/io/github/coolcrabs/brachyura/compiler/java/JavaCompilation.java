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

import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.util.Util;

public class JavaCompilation {
    private ArrayList<String> options = new ArrayList<>();
    private ArrayList<Path> sourceFiles = new ArrayList<>();
    private ArrayList<Path> sourcePath = new ArrayList<>();
    private ArrayList<Path> classpath = new ArrayList<>();
    private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public JavaCompilation addOption(String... options) {
        Collections.addAll(this.options, options);
        return this;
    }

    public JavaCompilation addSourceFile(Path path) {
        this.sourceFiles.add(path);
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

    public boolean compile(ProcessingSink sink) {
        try {
            try (BrachyuraJavaFileManager fileManager = new BrachyuraJavaFileManager()) {
                boolean result = false;
                fileManager.setLocation(StandardLocation.CLASS_PATH, bruh(classpath));
                fileManager.setLocation(StandardLocation.SOURCE_PATH, bruh(sourcePath));
                try (LoggerWriter w = new LoggerWriter()) {
                    CompilationTask compilationTask = compiler.getTask(w, fileManager, BrachyuraDiagnosticListener.INSTANCE, options, null, fileManager.getJavaFileObjectsFromFiles(bruh(sourceFiles)));
                    result = compilationTask.call();
                }
                fileManager.getProcessingSource().getInputs(sink);
                return result;
            }
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
}
