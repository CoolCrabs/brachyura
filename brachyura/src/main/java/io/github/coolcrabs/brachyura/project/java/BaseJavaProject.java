package io.github.coolcrabs.brachyura.project.java;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

import io.github.coolcrabs.brachyura.project.Project;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.javacompilelib.ForkingJavaCompilation;
import io.github.coolcrabs.javacompilelib.JavaCompilation;
import io.github.coolcrabs.javacompilelib.JavaCompilationUnit;

public abstract class BaseJavaProject extends Project {

    public boolean compile(JavaCompilationUnit javaCompilationUnit) {
        try {
            Path buildResourcesDir = getBuildResourcesDir();
            PathUtil.deleteDirectoryChildren(buildResourcesDir);
            if (!getCompiler().compile(javaCompilationUnit)) {
                return false;
            }
            return processResources(getResourcesDir(), getBuildResourcesDir());
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }
    
    public List<Path> getCompileDependencies() {
        return Collections.emptyList();
    }

    public boolean processResources(Path source, Path target) throws IOException {
        boolean[] result = new boolean[] {true};
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (processResource(source.relativize(file), file, target)) {
                    return FileVisitResult.CONTINUE;
                } else {
                    result[0] = false;
                    return FileVisitResult.TERMINATE;
                }
            }
        });
        return result[0];
    }

    public boolean processResource(Path relativePath, Path absolutePath, Path targetDir) throws IOException {
        Path target = targetDir.resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.copy(absolutePath, targetDir.resolve(relativePath));
        return true;
    }

    public Path getBuildClassesDir() {
        return PathUtil.resolveAndCreateDir(getBuildDir(), "classes");
    }

    public Path getBuildResourcesDir() {
        return PathUtil.resolveAndCreateDir(getBuildDir(), "resources");
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
        return new ForkingJavaCompilation(JvmUtil.CURRENT_JAVA_EXECUTABLE);
    }
}
