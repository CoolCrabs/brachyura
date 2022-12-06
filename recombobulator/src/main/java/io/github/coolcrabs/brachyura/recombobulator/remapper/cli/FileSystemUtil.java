package io.github.coolcrabs.brachyura.recombobulator.remapper.cli;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class FileSystemUtil {
    private FileSystemUtil() { }

    private static final Map<String, String> createArgs = Collections.singletonMap("create", "true");
    private static final FileSystemProvider jarFileSystemProvider;

    static {
        FileSystemProvider jarFileSystemProvider2 = null;
        for (FileSystemProvider fileSystemProvider : FileSystemProvider.installedProviders()) {
            if (fileSystemProvider.getScheme().equals("jar")) {
                jarFileSystemProvider2 = fileSystemProvider;
            }
        }
        Objects.requireNonNull(jarFileSystemProvider2);
        jarFileSystemProvider = jarFileSystemProvider2;
    }

    public static FileSystem newJarFileSystem(Path path) {
        try {
            return jarFileSystemProvider.newFileSystem(path, createArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
