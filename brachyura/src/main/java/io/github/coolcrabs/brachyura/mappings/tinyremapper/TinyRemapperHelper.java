package io.github.coolcrabs.brachyura.mappings.tinyremapper;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import net.fabricmc.tinyremapper.TinyRemapper;

public class TinyRemapperHelper {
    private TinyRemapperHelper() { }

    public enum JarType {
        CLASSPATH,
        INPUT
    }

    public static void read(TinyRemapper tr, Path jar, JarType type) throws IOException {
        try (FileSystem fileSystem = FileSystemUtil.newJarFileSystem(jar)) {
            read(tr, fileSystem, type);
        }
    }

    public static void read(TinyRemapper tr, FileSystem input, JarType type) throws IOException {
        List<Path> inputs = new ArrayList<>();
        Files.walkFileTree(input.getPath("/"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".class")) {
                    inputs.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if (type == JarType.CLASSPATH) {
            tr.readClassPath(inputs.toArray(new Path[inputs.size()]));
        } else {
            tr.readInputs(inputs.toArray(new Path[inputs.size()]));
        }
    }

    public static void copyNonClassfiles(FileSystem input, FileSystem output) throws IOException {
        Files.walkFileTree(input.getPath("/"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.toString().endsWith(".class")) {
                    Path target = output.getPath(file.toString());
                    Files.createDirectories(target.getParent());
                    Files.copy(file, target);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
