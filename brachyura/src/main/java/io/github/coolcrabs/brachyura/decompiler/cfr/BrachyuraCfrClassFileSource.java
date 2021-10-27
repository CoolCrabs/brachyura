package io.github.coolcrabs.brachyura.decompiler.cfr;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import io.github.coolcrabs.brachyura.util.JvmUtil;
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
        loadRt();
    }

    private void loadJar(Path path, @Nullable List<String> classesOut) throws IOException {
        FileSystem fileSystem = FileSystemUtil.newJarFileSystem(path);
        toClose.add(fileSystem);
        loadJar(fileSystem, classesOut);
    }

    private void loadJar(FileSystem fileSystem, @Nullable List<String> classesOut) throws IOException {
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

    private void loadRt() throws IOException {
        if (JvmUtil.CURRENT_JAVA_VERSION >= 9) {
            loadRtJ9();
        } else {
            loadRtJ8();
        }
    }

    private void loadRtJ8() throws IOException {
        String[] jars = System.getProperty("sun.boot.class.path").split(File.pathSeparator);
        for (String jar : jars) {
            Path path = Paths.get(jar);
            if (Files.exists(path)) { // ??? whatever sunrsasign.jar is claims to be on bootstrap classpath but doesn't exikst
                FileSystem fs;
                try {
                    fs = FileSystems.getFileSystem(new URI("jar:file", null, path.toUri().getPath(), ""));
                } catch (Exception e) {
                    fs = FileSystemUtil.newJarFileSystem(path);
                    toClose.add(fs);
                }
                loadJar(fs, null);
            }
        }
    }

    private void loadRtJ9() throws IOException {
        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        Files.walkFileTree(fs.getPath("modules"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String clazz = file.subpath(2, file.getNameCount()).toString();
                if (clazz.endsWith(".class") && !"module-info.class".equals(clazz)) {
                    allClasses.put(clazz, file);
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
