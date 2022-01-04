package io.github.coolcrabs.brachyura.decompiler.cfr;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
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

import io.github.coolcrabs.brachyura.decompiler.DecompileLineNumberTable;
import io.github.coolcrabs.brachyura.decompiler.LineNumberTableEntry;
import io.github.coolcrabs.brachyura.decompiler.DecompileLineNumberTable.MethodId;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import java.util.HashMap;
import java.util.Iterator;

class BrachyuraCfrOutputSinkFactory implements OutputSinkFactory, Closeable {
    private final FileSystem fileSystem;
    private final DecompileLineNumberTable decompileLineNumberTable;
    private final LineNumberMappingSink lineNumberMappingSink;
    private final @Nullable DecompiledSink decompiledSink;

    public BrachyuraCfrOutputSinkFactory(@Nullable Path outputJar, DecompileLineNumberTable mapping, boolean replace) {
        if (outputJar != null) {
            PathUtil.deleteIfExists(outputJar);
            fileSystem = FileSystemUtil.newJarFileSystem(outputJar);
            decompiledSink = new DecompiledSink(fileSystem);
        } else {
            fileSystem = null;
            decompiledSink = null;
        }
        decompileLineNumberTable = mapping;
        lineNumberMappingSink = new LineNumberMappingSink(replace);
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

    private class LineNumberMappingSink implements Sink<SinkReturns.LineNumberMapping> {
        final boolean replace;
        
        public LineNumberMappingSink(boolean replace) {
            this.replace = replace;
        }
        
        @Override
        public void write(LineNumberMapping sinkable) {
            Map<MethodId, DecompileLineNumberTable.MethodLineMap> a = decompileLineNumberTable.classes.computeIfAbsent(
                sinkable.className().replace('.', '/'),
                k -> new ConcurrentHashMap<>()
            );
            MethodId id = new MethodId(sinkable.methodName(), sinkable.methodDescriptor());
            if (replace) {
                List<LineNumberTableEntry> newLineNumbers = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : sinkable.getMappings().entrySet()) {
                    newLineNumbers.add(new LineNumberTableEntry(entry.getKey(), entry.getValue()));
                }
                a.put(id, new DecompileLineNumberTable.MethodLineMap(newLineNumbers));
            } else {
                // Bruh
                HashMap<Integer, Integer> remap = new HashMap<>();
                finish: {
                    Iterator<Map.Entry<Integer, Integer>> origOff2Line = sinkable.getClassFileMappings().entrySet().iterator();
                    Iterator<Map.Entry<Integer, Integer>> decompOff2Line = sinkable.getMappings().entrySet().iterator();
                    if (!decompOff2Line.hasNext()) break finish;
                    Map.Entry<Integer, Integer> currDecomp = decompOff2Line.next();
                    Map.Entry<Integer, Integer> nextDecomp = null;
                    while (origOff2Line.hasNext()) {
                        Map.Entry<Integer, Integer> origOff2LineE = origOff2Line.next();
                        for (;;) {
                            if (nextDecomp == null && decompOff2Line.hasNext()) {
                                nextDecomp = decompOff2Line.next();
                            }
                            if (nextDecomp != null && nextDecomp.getKey() <= origOff2LineE.getKey()) {
                                currDecomp = nextDecomp;
                                nextDecomp = null;
                            } else {
                                break;
                            }
                        }
                        remap.put(origOff2LineE.getValue(), currDecomp.getValue());
                    }
                }
                a.put(id, new DecompileLineNumberTable.MethodLineMap(remap));
            }
            
        }
    }

    @Override
    public void close() throws IOException {
        if (fileSystem != null) fileSystem.close();
    }
    
}
