package io.github.coolcrabs.brachyura.decompiler.cfr;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.jetbrains.annotations.Nullable;

import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.StreamUtil;

class BrachyuraCfrClassFileSource implements ClassFileSource, Closeable {
    private final Map<String, Path> allClasses = new HashMap<>();
    private final List<FileSystem> toClose = new ArrayList<>();

    public BrachyuraCfrClassFileSource(Path mainJar, List<Path> classpath, List<String> mainClassesOut) throws IOException {
        loadJar(mainJar, mainClassesOut);
        for (Path path : classpath) {
            loadJar(path, null);
        }
    }

    private void loadJar(Path path, @Nullable List<String> classesOut) throws IOException {
        FileSystem fileSystem = FileSystemUtil.newJarFileSystem(path);
        toClose.add(fileSystem);
        Files.walkFileTree(fileSystem.getPath("/"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".class")) {
                    allClasses.put(file.toString(), file);
                    if (classesOut != null) {
                        classesOut.add(file.toString());
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    public void informAnalysisRelativePathDetail(String usePath, String classFilePath) {
        //noop
    }

    @Override
    public Collection<String> addJar(String jarPath) {
        return Collections.emptyList();
    }

    @Override
    public String getPossiblyRenamedPath(String path) {
        return path;
    }

    @Override
    public Pair<byte[], String> getClassFileContent(String path) throws IOException {
        Path path2 = allClasses.get(path);
        if (path2 != null) {
            try (InputStream inputStream = PathUtil.inputStream(path2)) {
                return new Pair<>(StreamUtil.readFullyAsBytes(inputStream), path);
            }
        } else {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        for (FileSystem fileSystem : toClose) {
            fileSystem.close();
        }
    }
}
