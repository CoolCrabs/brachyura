package io.github.coolcrabs.brachyura.compiler.java;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;

import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;

public class JavaCompilationResult extends ProcessingSource {
    final BrachyuraJavaFileManager fileManager;
    final HashMap<ProcessingId, OutputFile> files = new HashMap<>(); 

    JavaCompilationResult(BrachyuraJavaFileManager s) {
        this.fileManager = s;
    }

    @Override
    public void getInputs(ProcessingSink sink) {
        for (Map.Entry<URI, OutputFile> entry : fileManager.output.entrySet()) {
            ProcessingId id = new ProcessingId(entry.getKey().getPath().substring(1), this);
            files.put(id, entry.getValue());
            sink.sink(entry.getValue()::openInputStream, id);
        }
    }

    public Path getSourceFile(ProcessingId id) {
        FileObject fileObject = files.get(id).sibling;
        if (fileObject == null) return null;
        URI uri = fileObject.toUri();
        if (!"file".equals(uri.getScheme())) {
            Logger.warn("Unknown source protocol in " + uri.toASCIIString());
            return null;
        }
        return Paths.get(uri);
    }
    
}
