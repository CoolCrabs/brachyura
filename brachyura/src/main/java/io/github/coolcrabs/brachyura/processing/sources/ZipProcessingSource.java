package io.github.coolcrabs.brachyura.processing.sources;

import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class ZipProcessingSource extends ProcessingSource implements Closeable {
    final FileSystem fs;
    
    public ZipProcessingSource(Path file) {
        if (!Files.exists(file)) throw Util.sneak(new FileNotFoundException(file.toString()));
        this.fs = FileSystemUtil.newJarFileSystem(file);
    }

    @Override
    public void getInputs(ProcessingSink sink) {
        try {
            Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    sink.sink(() -> PathUtil.inputStream(file), new ProcessingId(file.toString().substring(1), ZipProcessingSource.this));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw Util.sneak(ex);
        }
    }

    @Override
    public void close() {
        try {
            fs.close();
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
}
