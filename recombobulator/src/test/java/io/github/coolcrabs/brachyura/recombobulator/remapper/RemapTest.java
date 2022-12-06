package io.github.coolcrabs.brachyura.recombobulator.remapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.recombobulator.ClassInfo;
import io.github.coolcrabs.brachyura.recombobulator.FileSystemUtil;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.Obtainor;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOutput;
import io.github.coolcrabs.brachyura.recombobulator.TestUtil;
import io.github.coolcrabs.brachyura.recombobulator.remapper.RecombobulatorRemapper.Input;
import io.github.coolcrabs.brachyura.recombobulator.remapper.RecombobulatorRemapper.OutputFile;
import io.github.coolcrabs.brachyura.recombobulator.remapper.cli.Lazy;
import io.github.coolcrabs.brachyura.recombobulator.remapper.cli.PathIsSup;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class RemapTest {

    @Test
    void remapMc() throws IOException {
        Path bruh = Obtainor.getMc();
        Path inter = Obtainor.getInt();
        MemoryMappingTree tree = new MemoryMappingTree();
        MappingReader.read(inter, MappingFormat.TINY, tree);
        long start = System.nanoTime();
        Path outyeet = TestUtil.TMP.resolve(System.currentTimeMillis() + ".jar");
        RecombobulatorOptions options = new RecombobulatorOptions();
        try (
            FileSystem fs = FileSystemUtil.newJarFileSystem(bruh);
            FileSystem outfs = FileSystemUtil.newJarFileSystem(outyeet)
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
            Mappings mappings2 = new MappingIoMappings(tree, tree.getNamespaceId("official"), tree.getNamespaceId("intermediary"), imap);
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
        long end = System.nanoTime();
        System.out.println(new BigDecimal(end - start).divide(new BigDecimal(1000000000)));
    }

    @Test
    void fieldDescBug() {
        Mappings mappings = new Mappings() {
            @Override
            public Mutf8Slice mapClass(Mutf8Slice srcCls) {
                if (srcCls.toString().equals("dry")) {
                    return new Mutf8Slice("thonk");
                }
                return srcCls;
            }
        };
        assertEquals(new Mutf8Slice("[Lthonk;"), Mappings.remapFieldDescriptor(mappings, new Mutf8Slice("[Ldry;")));
    }

    @Test
    void sigRemap() {
        Mutf8Slice sig = new Mutf8Slice("<T::Ldso<TT;>;>Ljava/lang/Object;");
        HashMap<Mutf8Slice, Mutf8Slice> classes = new HashMap<>();
        Mappings mappings = new Mappings() {
            @Override
            public Mutf8Slice mapClass(Mutf8Slice srcCls) {
                return classes.getOrDefault(srcCls, srcCls);
            }
        };
        put(classes, "dso", "thonk");
        assertEquals("<T::Lthonk<TT;>;>Ljava/lang/Object;", Mappings.remapSignature(mappings, sig).toString());
        Mutf8Slice sig2 = new Mutf8Slice("(Lgi<TA;>.a;Lqx;)V");
        put(classes, "gi", "net/minecraft/class_2319");
        put(classes, "gi$a", "net/minecraft/class_2319$class_7219");
        put(classes, "qx", "net/minecraft/class_2540");
        assertEquals("(Lnet/minecraft/class_2319<TA;>.class_7219;Lnet/minecraft/class_2540;)V", Mappings.remapSignature(mappings, sig2).toString());
        assertEquals("<T:Ljava/lang/Object;>Ljava/lang/Record;", Mappings.remapSignature(mappings, new Mutf8Slice("<T:Ljava/lang/Object;>Ljava/lang/Record;")).toString());
    }

    void put(HashMap<Mutf8Slice, Mutf8Slice> classes, String in, String out) {
        classes.put(new Mutf8Slice(in), new Mutf8Slice(out));
    }
}
