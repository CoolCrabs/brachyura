package io.github.coolcrabs.brachyura.compiler.java;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import javax.tools.JavaFileManager.Location;

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
        getOutputLocation(StandardLocation.CLASS_OUTPUT, sink);
    }

    public void getOutputLocation(Location location, ProcessingSink sink) {
        String locHost = location.getName().replaceAll("[^a-zA-Z0-9]", ".");
        for (Map.Entry<URI, OutputFile> entry : fileManager.output.entrySet()) {
            if (!entry.getValue().exists || !entry.getKey().getHost().equals(locHost)) continue;
            ProcessingId id = new ProcessingId(entry.getKey().getPath().substring(1), this);
            files.put(id, entry.getValue());
            sink.sink(entry.getValue()::openInputStream, id);
        }
    }

    /**
     * Gets the source file that produced an output
     * @param id
     * @return The source file an output came from or null if it is not from a source file (from an annotation processor etc)
     */
    public @Nullable Path getSourceFile(ProcessingId id) {
        FileObject fileObject = files.get(id).sibling;
        if (fileObject == null) return null;
        URI uri = fileObject.toUri();
        if ("file".equals(uri.getScheme())) {
            return Paths.get(uri);
        }
        if (!"crabmoment".equals(uri.getScheme())) {
            Logger.warn("Unknown source protocol in " + uri.toASCIIString());
        }
        return null;
    }
    
}
