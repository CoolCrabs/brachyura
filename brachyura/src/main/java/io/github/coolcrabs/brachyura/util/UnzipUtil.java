package io.github.coolcrabs.brachyura.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// Based on https://mkyong.com/java/how-to-decompress-files-from-a-zip-file/
public class UnzipUtil {
    private UnzipUtil() { }

    public static void unzipToDir(Path sourceFile, Path targetDir) {
        try {
            try (ZipInputStream zis = new ZipInputStream(PathUtil.inputStream(sourceFile))) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    boolean isDirectory = false;
                    // some zip stored files and folders separately
                    // e.g data/
                    //     data/folder/
                    //     data/folder/file.txt
                    if (zipEntry.getName().endsWith("/")) { // Zip files use unix seperator
                        isDirectory = true;
                    }
                    Path newPath = zipSlipProtect(zipEntry, targetDir);
                    if (isDirectory) {
                        Files.createDirectories(newPath);
                    } else {
                        // some zip stored file path only, need create parent directories
                        // e.g data/folder/file.txt
                        if (newPath.getParent() != null) {
                            if (Files.notExists(newPath.getParent())) {
                                Files.createDirectories(newPath.getParent());
                            }
                        }
                        Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }

    private static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException {
        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }
}
