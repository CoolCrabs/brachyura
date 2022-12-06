package io.github.coolcrabs.brachyura.recombobulator.remapper.cli;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.function.Supplier;

import io.github.coolcrabs.brachyura.recombobulator.ClassInfo;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.remapper.InheritanceMap;
import io.github.coolcrabs.brachyura.recombobulator.remapper.MappingIoMappings;
import io.github.coolcrabs.brachyura.recombobulator.remapper.Mappings;
import io.github.coolcrabs.brachyura.recombobulator.remapper.RecombobulatorRemapper;
import io.github.coolcrabs.brachyura.recombobulator.remapper.RecombobulatorRemapper.Input;
import io.github.coolcrabs.brachyura.recombobulator.remapper.RecombobulatorRemapper.OutputFile;
import io.github.coolcrabs.brachyura.recombobulator.remapper.RemapperOutputConsumer;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            System.err.println("Usage: inputjar outputjar mappingsfile innamespace outnamespace");
        }
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1].equals("auto") ? "" + System.currentTimeMillis() : args[1]);
        Path mappings = Paths.get(args[2]);
        String inNamespace = args[3];
        String outNamepsace = args[4];
        MemoryMappingTree tree = new MemoryMappingTree();
        MappingReader.read(mappings, tree);
        RecombobulatorOptions options = new RecombobulatorOptions();
        try (
            FileSystem fs = FileSystemUtil.newJarFileSystem(input);
            FileSystem outfs = FileSystemUtil.newJarFileSystem(output)
        ) {
            //TODO: evalutate data structure
            LinkedList<Entry<Mutf8Slice, Supplier<ClassInfo>>> inherClasses = new LinkedList<>();
            LinkedList<Input> inputs = new LinkedList<>();
            LinkedList<OutputFile> otherFiles = new LinkedList<>();
            Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileString = file.toString().substring(1);
                    if (fileString.endsWith(".class")) {
                        Supplier<ClassInfo> clsSup = new Lazy(() -> {
                            try {
                                return new ClassInfo(ByteBuffer.wrap(Files.readAllBytes(file)), options);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });
                        inherClasses.add(new AbstractMap.SimpleImmutableEntry<>(new Mutf8Slice(fileString.substring(0, fileString.length() - 6)), clsSup));
                        inputs.add(new Input(clsSup, fileString, null));
                    } else {
                        otherFiles.add(new OutputFile(new PathIsSup(file), fileString, null));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            InheritanceMap imap = new InheritanceMap();
            imap.load(inherClasses, true);
            Mappings mappings2 = new MappingIoMappings(tree, tree.getNamespaceId(inNamespace), tree.getNamespaceId(outNamepsace), imap);
            RecombobulatorRemapper remapper = new RecombobulatorRemapper();
            remapper.setClasses(inputs);
            remapper.setFiles(otherFiles);
            remapper.setMappings(mappings2);
            remapper.setOutput(new RemapperOutputConsumer() {
                @Override
                public void outputClass(String path, ClassInfo ci, Object tag) {
                    try {
                        Path p = outfs.getPath(path);
                        Path pparent = p.getParent();
                        if (pparent != null) {
                            Files.createDirectories(pparent);
                        }
                        try (
                            SeekableByteChannel ch = Files.newByteChannel(p, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)
                        ) {
                            ByteBuffer b = ByteBuffer.allocateDirect(ci.byteSize());
                            ci.write(RecombobulatorOutput.of(b));
                            b.position(0);
                            ch.write(b);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }

                @Override
                public void outputFile(String path, Supplier<InputStream> ci, Object tag) {
                    try {
                        Path p = outfs.getPath(path);
                        Path pparent = p.getParent();
                        if (pparent != null) {
                            Files.createDirectories(pparent);
                        }
                        Files.copy(((PathIsSup)ci).p, p);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
            remapper.run();
        }
    }
}
