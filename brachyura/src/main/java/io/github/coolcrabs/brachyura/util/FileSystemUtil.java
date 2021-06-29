package io.github.coolcrabs.brachyura.util;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileSystemUtil {
    private FileSystemUtil() { }

    private static final Map<String, String> createArgs = new HashMap<>();
    private static final FileSystemProvider jarFileSystemProvider;

    static {
        createArgs.put("create", "true");
        FileSystemProvider jarFileSystemProvider2 = null;
        for (FileSystemProvider fileSystemProvider : FileSystemProvider.installedProviders()) {
            if (fileSystemProvider.getScheme().equals("jar")) {
                jarFileSystemProvider2 = fileSystemProvider;
            }
        }
        Objects.requireNonNull(jarFileSystemProvider2);
        jarFileSystemProvider = jarFileSystemProvider2;
    }

    public static FileSystem newFileSystem(Path path) {
        try {
            return jarFileSystemProvider.newFileSystem(path, createArgs);
        } catch (Exception e) {
            throw Util.sneak(e);
        }
    }
}
