package io.github.coolcrabs.brachyura.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.GZIPOutputStream;

public class PathUtil {
    private PathUtil() { }

    public static final Path HOME = Paths.get(System.getProperty("user.home"));
    public static final Path CWD = Paths.get("").toAbsolutePath();

    public static Path brachyuraPath() {
        return HOME.resolve(".brachyura");
    }

    public static Path cachePath() {
        return brachyuraPath().resolve("cache");
    }

    public static Path resolveAndCreateDir(Path parent, String child) {
        try {
            Path result = parent.resolve(child);
            Files.createDirectories(result);
            return result;
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static void deleteDirectoryChildren(Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static void copyDir(Path source, Path target) {
        Path a = pathTransform(target.getFileSystem(), source);
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = target.resolve(a.relativize(pathTransform(target.getFileSystem(), file)));
                    Files.createDirectories(targetFile.getParent());
                    Files.copy(file, targetFile);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    // https://stackoverflow.com/a/22611925
    public static Path pathTransform(FileSystem fs, final Path path) {
        Path ret = fs.getPath(path.isAbsolute() ? fs.getSeparator() : "");
        for (Path component : path) {
            ret = ret.resolve(component.getFileName().toString());
        }
        return ret;
    }

    public static InputStream inputStream(Path path) {
        try {
            return new BufferedInputStream(Files.newInputStream(path));
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static OutputStream outputStream(Path path) {
        try {
            Files.createDirectories(path.getParent());
            return new BufferedOutputStream(Files.newOutputStream(path));
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    /**
     * Returns a temp file in the same directory as the target file
     */
    public static Path tempFile(Path target) {
        try {
            Files.createDirectories(target.getParent());
            return Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static Path tempDir(Path target) {
        try {
            Files.createDirectories(target.getParent());
            return Files.createTempDirectory(target.getParent(), target.getFileName().toString());
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static void moveAtoB(Path a, Path b) {
        try {
            Files.move(a, b, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            try {
                Files.delete(b);
            } catch (Exception e2) {
                // File prob wasn't created
            }
            throw Util.sneak(e);
        }
    }

    public static void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static void deleteDirectory(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static BufferedWriter newBufferedWriter(Path path) {
        try {
            Files.createDirectories(path.getParent());
            return Files.newBufferedWriter(path);
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static BufferedWriter newBufferedWriter(Path path, OpenOption... options) {
        try {
            Files.createDirectories(path.getParent());
            return Files.newBufferedWriter(path, options);
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static BufferedReader newBufferedReader(Path path) {
        try {
            return Files.newBufferedReader(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static BufferedWriter newGzipBufferedWriter(Path path) {
        try {
            Files.createDirectories(path.getParent());
            return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(path)), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    public static void createDirectories(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
}
