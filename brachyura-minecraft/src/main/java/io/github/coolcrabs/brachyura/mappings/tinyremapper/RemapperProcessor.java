package io.github.coolcrabs.brachyura.mappings.tinyremapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingId;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.processing.Processor;
import io.github.coolcrabs.brachyura.recombobulator.ClassInfo;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.remapper.InheritanceMap;
import io.github.coolcrabs.brachyura.recombobulator.remapper.MappingIoMappings;
import io.github.coolcrabs.brachyura.recombobulator.remapper.Mappings;
import io.github.coolcrabs.brachyura.recombobulator.remapper.RecombobulatorRemapper;
import io.github.coolcrabs.brachyura.recombobulator.remapper.RemapperOutputConsumer;
import io.github.coolcrabs.brachyura.recombobulator.remapper.RecombobulatorRemapper.Input;
import io.github.coolcrabs.brachyura.util.CloseableArrayList;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.tree.MappingTree;

public class RemapperProcessor implements Processor {
    public Mappings mappings;
    final List<Path> classpath;
    final MappingTree mappingTree;
    final int src;
    final int dst;

    public RemapperProcessor(List<Path> classpath, MappingTree mappingTree, int src, int dst) {
        this.classpath = classpath;
        this.mappingTree = mappingTree;
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
        LinkedList<Entry<Mutf8Slice, Supplier<ClassInfo>>> inherClasses = new LinkedList<>();
        ArrayList<Input> ins = new ArrayList<>(inputs.size());
        RecombobulatorOptions options = new RecombobulatorOptions();
        try (CloseableArrayList toClose = new CloseableArrayList()) {
            for (Path j : classpath) {
                Path root;
                if (Files.isDirectory(j)) {
                    root = j;
                } else {
                    FileSystem fs = FileSystemUtil.newJarFileSystem(j);
                    root = fs.getPath("/");
                    toClose.add(fs);
                }
                Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toString().endsWith(".class")) {
                            Path r = root.relativize(file);
                            StringBuilder b = new StringBuilder();
                            for (Path ppart : r) {
                                b.append(ppart.toString());
                                b.append('/');
                            }
                            b.setLength(b.length() - 7);
                            inherClasses.add(new AbstractMap.SimpleImmutableEntry<>(new Mutf8Slice(b.toString()), () -> {
                                try {
                                    return new ClassInfo(ByteBuffer.wrap(Files.readAllBytes(file)), options);
                                } catch (IOException e) {
                                    throw Util.sneak(e);
                                }
                            }));
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            for (ProcessingEntry e : inputs) {
                if (e.id.path.endsWith(".class")) {
                    Lazy<ClassInfo> read = new Lazy<>(() -> {
                        try (InputStream is = e.in.get()) {
                            return new ClassInfo(ByteBuffer.wrap(StreamUtil.readFullyAsBytes(is)), options);
                        } catch (IOException e0) {
                            throw Util.sneak(e0);
                        }
                    });
                    inherClasses.add(new AbstractMap.SimpleImmutableEntry<>(new Mutf8Slice(e.id.path.substring(0, e.id.path.length() - 6)), read));
                    ins.add(new Input(read, e.id.path, e.id.source));
                } else {
                    sink.sink(e.in, e.id);
                }
            }
            InheritanceMap imap = new InheritanceMap();
            imap.load(inherClasses, true);
            mappings = new MappingIoMappings(mappingTree, src, dst, imap);
            RecombobulatorRemapper remapper = new RecombobulatorRemapper();
            remapper.setClasses(ins);
            remapper.setMappings(mappings);
            ConcurrentLinkedQueue<ProcessingEntry> o = new ConcurrentLinkedQueue<>();
            remapper.setOutput(new RemapperOutputConsumer() {
                public void outputClass(String path, ClassInfo ci, Object tag) {
                    byte[] out = new byte[ci.byteSize()];
                    ci.write(RecombobulatorOutput.of(ByteBuffer.wrap(out)));
                    o.add(new ProcessingEntry(() -> new ByteArrayInputStream(out), new ProcessingId(path, (ProcessingSource)tag)));
                }
    
                public void outputFile(String path, Supplier<InputStream> isSup, Object tag) {
                    //noop
                }
            });
            remapper.run();
            for (ProcessingEntry e : o) sink.sink(e.in, e.id);
        }
    }
}
