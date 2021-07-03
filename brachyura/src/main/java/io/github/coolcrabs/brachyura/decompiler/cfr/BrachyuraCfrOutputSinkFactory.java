package io.github.coolcrabs.brachyura.decompiler.cfr;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns;
import org.benf.cfr.reader.api.SinkReturns.Decompiled;
import org.benf.cfr.reader.api.SinkReturns.ExceptionMessage;
import org.benf.cfr.reader.api.SinkReturns.LineNumberMapping;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import io.github.coolcrabs.brachyura.decompiler.ReplaceLineNumberMappings;
import io.github.coolcrabs.brachyura.decompiler.ReplaceLineNumberMappings.LineNumberMappingEntry;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;

class BrachyuraCfrOutputSinkFactory implements OutputSinkFactory, Closeable {
    private final FileSystem fileSystem;
    private final Writer lineNumberMappingsWriter;
    private final Map<String, List<LineNumberMappingEntry>> lineNumberMappings = new ConcurrentHashMap<>();
    private final @Nullable LineNumberMappingSink lineNumberMappingSink;
    private final @Nullable DecompiledSink decompiledSink;
    

    public BrachyuraCfrOutputSinkFactory(@Nullable Path outputJar, @Nullable Path lineNumberMappingsPath) {
        PathUtil.deleteIfExists(outputJar);
        fileSystem = FileSystemUtil.newJarFileSystem(outputJar);
        decompiledSink = outputJar == null ? null : new DecompiledSink(fileSystem);
        if (lineNumberMappingsPath != null) {
            lineNumberMappingsWriter = PathUtil.newGzipBufferedWriter(lineNumberMappingsPath);
            lineNumberMappingSink = new LineNumberMappingSink(lineNumberMappings);
        } else {
            lineNumberMappingsWriter = null;
            lineNumberMappingSink = null;
        }
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

    @SuppressWarnings("unchecked")
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
            if (sinkable.getClassName().indexOf('$') < 0) { // skip inner classes because they are included in the parent source file 
                try {
                    Path path = fileSystem.getPath("/" + sinkable.getPackageName().replace('.', '/') + "/" + sinkable.getClassName() + ".java");
                    Files.createDirectories(path.getParent());
                    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                        writer.write(sinkable.getJava());
                    }
                } catch (IOException e) {
                    throw Util.sneak(e);
                }
            }
        }
    }

    private static class LineNumberMappingSink implements Sink<SinkReturns.LineNumberMapping> {
        final Map<String, List<LineNumberMappingEntry>> lineNumberMappings;
        LineNumberMappingSink(Map<String, List<LineNumberMappingEntry>> lineNumberMappings) {
            this.lineNumberMappings = lineNumberMappings;
        }

        @Override
        public void write(LineNumberMapping sinkable) {
            lineNumberMappings.computeIfAbsent(sinkable.className().replace('.', '/'), k -> new ArrayList<>()).add(new LineNumberMappingEntry(sinkable.methodName(), sinkable.methodDescriptor(), sinkable.getMappings()));
        }
    }

    @Override
    public void close() throws IOException {
        fileSystem.close();
        new ReplaceLineNumberMappings(lineNumberMappings).write(lineNumberMappingsWriter);
        lineNumberMappingsWriter.close();
    }
    
}
