package io.github.coolcrabs.brachyura.decompiler.cfr;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;

import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;
import org.benf.cfr.reader.api.SinkReturns.Decompiled;
import org.benf.cfr.reader.api.SinkReturns.ExceptionMessage;
import org.benf.cfr.reader.api.SinkReturns.LineNumberMapping;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

class BrachyuraCfrOutputSinkFactory implements OutputSinkFactory, Closeable {
    private final FileSystem fileSystem;
    private final @Nullable LineNumberMappingSink lineNumberMappingSink;
    private final DecompiledSink decompiledSink;
    

    public BrachyuraCfrOutputSinkFactory(@Nullable Path outputJar, @Nullable Path lineNumberMappings) {
        PathUtil.deleteIfExists(outputJar);
        fileSystem = FileSystemUtil.newJarFileSystem(outputJar);
        decompiledSink = outputJar == null ? null : new DecompiledSink(fileSystem);
        lineNumberMappingSink = lineNumberMappings == null ? null : new LineNumberMappingSink();
    }

    @Override
    public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> available) {
        ArrayList<SinkClass> result = new ArrayList<>();
        if (lineNumberMappingSink != null && sinkType == SinkType.LINENUMBER) {
            result.add(SinkClass.LINE_NUMBER_MAPPING);
        }
        if (decompiledSink != null && sinkType == SinkType.JAVA) {
            result.add(SinkClass.DECOMPILED);
        }
        if (sinkType == SinkType.EXCEPTION) {
            result.add(SinkClass.EXCEPTION_MESSAGE);
        }
        return result;
    }

    @Override
    public <T> Sink<T> getSink(SinkType sinkType, SinkClass sinkClass) {
        if (sinkClass == SinkClass.LINE_NUMBER_MAPPING) {
            return (Sink<T>) lineNumberMappingSink;
        }
        if (sinkClass == SinkClass.DECOMPILED) {
            return (Sink<T>) decompiledSink;
        }
        if (sinkClass == SinkClass.EXCEPTION_MESSAGE) {
            return (Sink<T>) ExceptionMessageSink.INSTANCE;
        }
        return ignored -> {};
    }

    private enum ExceptionMessageSink implements Sink<SinkReturns.ExceptionMessage> {
        INSTANCE;

        @Override
        public void write(ExceptionMessage sinkable) {
            Logger.warn("Exception Decompiling " + sinkable.getPath());
            Logger.warn(sinkable.getThrownException());
        }
    }

    private static class DecompiledSink implements Sink<SinkReturns.Decompiled> {
        final FileSystem fileSystem;
        DecompiledSink(FileSystem fileSystem) {
            this.fileSystem = fileSystem;
        }

        @Override
        public void write(Decompiled sinkable) {
            try {
                Path path = fileSystem.getPath("/" + sinkable.getPackageName().replace('.', '/') + "/" + sinkable.getClassName() + ".java");
                Files.createDirectories(path.getParent());
                try (BufferedWriter writer = Files.newBufferedWriter(path)) { // implicit utf8
                    writer.write(sinkable.getJava());
                }
            } catch (IOException e) {
                throw Util.sneak(e);
            }
        }
    }

    private static class LineNumberMappingSink implements Sink<SinkReturns.LineNumberMapping> {
        Gson gson = new Gson();

        @Override
        public void write(LineNumberMapping sinkable) {
            LineNumberMappings lineNumberMappings = new LineNumberMappings();
            lineNumberMappings.methodName = sinkable.methodName();
            lineNumberMappings.methodDescriptor = sinkable.methodDescriptor();
            lineNumberMappings.mappings = sinkable.getMappings();
            Logger.info(gson.toJson(lineNumberMappings));
        }
    }

    @Override
    public void close() throws IOException {
        fileSystem.close();
    }
    
}
