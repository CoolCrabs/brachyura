package io.github.coolcrabs.brachyura.processing.sinks;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.PathUtil;

public class AtomicZipProcessingSink implements ProcessingSink, AutoCloseable {
    final AtomicFile file;
    ZipProcessingSink delegate;

    public AtomicZipProcessingSink(Path zip) {
        file = new AtomicFile(zip);
        PathUtil.deleteIfExists(file.tempPath);
        delegate = new ZipProcessingSink(file.tempPath);
    }

    @Override
    public void sink(Supplier<InputStream> in, ProcessingId id) {
        delegate.sink(in, id);
    }

    public void commit() {
        delegate.close();
        delegate = null;
        file.commit();
    }

    @Override
    public void close() {
        if (delegate != null) {
            delegate.close();
        }
        file.close();
    }
    
}
