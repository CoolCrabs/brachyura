package io.github.coolcrabs.brachyura.compiler.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.javacompilelib.JavaCompilationUnit;

public class JavaCompilationUnitBuilder extends JavaCompilationUnit.Builder {

    public JavaCompilationUnitBuilder sourceDir(Path... dirs) {
        try {
            List<File> sourceFiles = new ArrayList<>();
            for (Path dir : dirs) {
                Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().endsWith(".java")) {
                            sourceFiles.add(file.toFile());
                        } else {
                            Logger.warn("Unrecognized file type for file {} in java src dir", file);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            return this.sourceFiles(sourceFiles.toArray(new File[sourceFiles.size()]));
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }
    
    @Override
    public JavaCompilationUnitBuilder sourceFiles(File[] sourceFiles) {
        super.sourceFiles(sourceFiles);
        return this;
    }

    public JavaCompilationUnitBuilder outputDir(Path path) {
        return this.outputDir(path.toFile());
    }

    @Override
    public JavaCompilationUnitBuilder outputDir(File outputDir) {
        super.outputDir(outputDir);
        return this;
    }

    public JavaCompilationUnitBuilder classpath(List<Path> classpath) {
        File[] a = new File[classpath.size()];
        for (int i = 0; i < classpath.size(); i++) {
            a[i] = classpath.get(i).toFile();
        }
        return classpath(a);
    }

    @Override
    public JavaCompilationUnitBuilder classpath(File[] classpath) {
        super.classpath(classpath);
        return this;
    }

    @Override
    public JavaCompilationUnitBuilder annotationProcessors(String[] annotationProcessors) {
        super.annotationProcessors(annotationProcessors);
        return this;
    }

    @Override
    public JavaCompilationUnitBuilder options(String[] options) {
        super.options(options);
        return this;
    }
}
