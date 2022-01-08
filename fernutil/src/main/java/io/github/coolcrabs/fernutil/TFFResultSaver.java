package io.github.coolcrabs.fernutil;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.jar.Manifest;

import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.tinylog.Logger;

import io.github.coolcrabs.fernutil.FernUtil.LineNumbers;

class TFFResultSaver implements IResultSaver, Closeable {
    FileSystem fs;
    Consumer<LineNumbers> lines;

    public TFFResultSaver(Path out, Consumer<LineNumbers> lines) {
        fs = TUtil.newJarFileSystem(out);
        this.lines = lines;
    }

    // Override in forgeflower
    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content, int[] mapping) {
        try {
            Path o = fs.getPath(qualifiedName + ".java");
            if (o.getParent() != null) Files.createDirectories(o.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(o)) {
                w.write(content);
            }
            lines.accept(new LineNumbers(qualifiedName, mapping));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void saveFolder(String path) {
        // stub
    }

    @Override
    public void copyFile(String source, String path, String entryName) {
        // stub
    }

    @Override
    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
        // stub
    }

    @Override
    public void createArchive(String path, String archiveName, Manifest manifest) {
        // stub
    }

    @Override
    public void saveDirEntry(String path, String archiveName, String entryName) {
        // stub
    }

    @Override
    public void copyEntry(String source, String path, String archiveName, String entry) {
        // stub
    }

    @Override
    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
        Logger.warn("No mappings for {}", qualifiedName);
        saveClassEntry(path, archiveName, qualifiedName, entryName, content, null);
    }

    @Override
    public void closeArchive(String path, String archiveName) {
        // stub
    }

    @Override
    public void close() throws IOException {
        fs.close();
    }
    
}
