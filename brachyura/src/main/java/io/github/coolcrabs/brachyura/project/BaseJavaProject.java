package io.github.coolcrabs.brachyura.project;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

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
        
            Path resourcesDir = getResourcesDir();
            Files.walkFileTree(resourcesDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    processResource(resourcesDir.relativize(file), file, buildResourcesDir);
                    return FileVisitResult.CONTINUE;
                }
            });

            return true;
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }
    
    public List<Path> getCompileDependencies() {
        return Collections.emptyList();
    }

    public void processResource(Path relativePath, Path absolutePath, Path targetDir) throws IOException {
        Path target = targetDir.resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.copy(absolutePath, targetDir.resolve(relativePath));
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
