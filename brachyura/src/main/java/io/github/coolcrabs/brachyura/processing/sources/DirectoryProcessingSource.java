package io.github.coolcrabs.brachyura.processing.sources;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

public class DirectoryProcessingSource extends ProcessingSource {
    final Path path;

    public DirectoryProcessingSource(Path path) {
        this.path = path;
    }

    @Override
    public void getInputs(ProcessingSink sink) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path r = path.relativize(file);
                    StringBuilder b = new StringBuilder();
                    for (Path ppart : r) {
                        b.append(ppart.toString());
                        b.append('/');
                    }
                    b.setLength(b.length() - 1);
                    sink.sink(() -> PathUtil.inputStream(file), new ProcessingId(b.toString(), DirectoryProcessingSource.this));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw Util.sneak(e);
        }
    }
    
}
