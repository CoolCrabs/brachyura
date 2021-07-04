package io.github.coolcrabs.brachyura.compiler.java;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.util.Util;

//TODO: how to implement annotation processors?

public class JavaCompilers {
    // public static final boolean forking = Boolean.getBoolean("brachyura.forkingjavac"); // TODO: implement

    private static final JavaCompiler JAVA_COMPILER = ToolProvider.getSystemJavaCompiler();

    public static boolean compile(Path sourceDir, Path outputDir, List<Path> classpath) {
        try {
            StandardJavaFileManager fileManager = JAVA_COMPILER.getStandardFileManager(null, null, StandardCharsets.UTF_8);
            fileManager.setLocation(StandardLocation.CLASS_PATH, whyAmITargetingJava8Again(classpath));
            List<File> sourceFiles = new ArrayList<>();
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
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
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(outputDir.toFile()));
            CompilationTask compilationTask = JAVA_COMPILER.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjectsFromFiles(sourceFiles));
            return compilationTask.call();
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }

    private static List<File> whyAmITargetingJava8Again(List<Path> paths) {
        ArrayList<File> result = new ArrayList<>(paths.size());
        for (Path path : paths) {
            result.add(path.toFile());
        }
        return result;
    }
}
