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

import org.jetbrains.annotations.Nullable;

import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import net.fabricmc.tinyremapper.InputTag;
import net.fabricmc.tinyremapper.TinyRemapper;

public class TinyRemapperHelper {
    private TinyRemapperHelper() { }

    public static final String VERSION = TinyRemapper.class.getPackage().getImplementationVersion();

    public static String getFileVersionTag() {
        return "-TRv" + VERSION + "-"; 
    }

    public enum JarType {
        CLASSPATH,
        INPUT
    }

    public static void readJar(TinyRemapper tr, Path jar, JarType type) throws IOException {
        try (FileSystem fileSystem = FileSystemUtil.newJarFileSystem(jar)) {
            readFileSystem(tr, fileSystem, type, null);
        }
    }

    public static void readFileSystem(TinyRemapper tr, FileSystem input, JarType type) throws IOException {
        readFileSystem(tr, input, type, null);
    }

    public static void readFileSystem(TinyRemapper tr, FileSystem input, JarType type, @Nullable InputTag tag) throws IOException {
        readDir(tr, input.getPath("/"), type, tag);
    }

    public static void readDir(TinyRemapper tr, Path inputDir, JarType type) throws IOException {
        readDir(tr, inputDir, type, null);
    }
    
    public static void readDir(TinyRemapper tr, Path inputDir, JarType type, @Nullable InputTag tag) throws IOException {
        List<Path> inputs = new ArrayList<>();
        Files.walkFileTree(inputDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".class")) {
                    inputs.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if (type == JarType.CLASSPATH) {
            if (tag != null) throw new UnsupportedOperationException();
            tr.readClassPath(inputs.toArray(new Path[inputs.size()]));
        } else {
            tr.readInputs(tag, inputs.toArray(new Path[inputs.size()]));
        }
    }

    public static void copyNonClassfilesFromFileSystem(FileSystem input, FileSystem output) throws IOException {
        copyNonClassfilesFromDir(input.getPath("/"), output);
    }

    public static void copyNonClassfilesFromDir(Path dir, FileSystem output) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.toString().endsWith(".class")) {
                    Path target = output.getPath("/").resolve(dir.relativize(file).toString());
                    Files.createDirectories(target.getParent());
                    Files.copy(file, target);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
