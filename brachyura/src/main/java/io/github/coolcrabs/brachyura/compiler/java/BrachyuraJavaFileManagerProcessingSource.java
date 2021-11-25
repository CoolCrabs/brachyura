package io.github.coolcrabs.brachyura.compiler.java;

import java.net.URI;
import java.util.Map;

import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;

class BrachyuraJavaFileManagerProcessingSource extends ProcessingSource {
    final BrachyuraJavaFileManager fileManager;

    BrachyuraJavaFileManagerProcessingSource(BrachyuraJavaFileManager s) {
        this.fileManager = s;
    }

    @Override
    public void getInputs(ProcessingSink sink) {
        for (Map.Entry<URI, BrachyuraJavaFileManager.OutputFile> entry : fileManager.output.entrySet()) {
            sink.sink(entry.getValue()::openInputStream, new ProcessingId(entry.getKey().getPath().substring(1), this));
        }
    }
    
}
