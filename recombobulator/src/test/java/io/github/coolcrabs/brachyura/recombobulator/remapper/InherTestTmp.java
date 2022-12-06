package io.github.coolcrabs.brachyura.recombobulator.remapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.recombobulator.ClassInfo;
import io.github.coolcrabs.brachyura.recombobulator.FileSystemUtil;
import io.github.coolcrabs.brachyura.recombobulator.Mutf8Slice;
import io.github.coolcrabs.brachyura.recombobulator.Obtainor;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.remapper.InheritanceMap.InheritanceGroup;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class InherTestTmp {
    // @Test
    // void e() throws IOException {
    //     int[] cnt = new int[1];
    //     Map<Mutf8Slice, Supplier<ClassInfo>> classes = new HashMap<>();
    //     try (
    //         FileSystem fs = FileSystemUtil.newJarFileSystem(Obtainor.getMc())
    //     ) {
    //         Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>(){
    //             @Override
    //             public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    //                 String fileString = file.toString();
    //                 RecombobulatorOptions options = new RecombobulatorOptions();
    //                 if (fileString.endsWith(".class")) {
    //                     Mutf8Slice clsName = new Mutf8Slice(fileString.substring(1).replace(".class", ""));
    //                     ClassInfo ci = new ClassInfo(ByteBuffer.wrap(Files.readAllBytes(file)), options);
    //                     cnt[0] += ci.methods.size();
    //                     classes.put(clsName, () -> ci);
    //                 }
    //                 return FileVisitResult.CONTINUE;
    //             }
    //         });
    //     }
    //     System.out.println(cnt[0]);
    //     long start = System.nanoTime();
    //     InheritanceMap imap = new InheritanceMap();
    //     imap.load(classes, true);
    //     long end = System.nanoTime();
    //     System.out.println(new BigDecimal(end - start).divide(new BigDecimal(1000000000)));
    //     int i = 0;
    //     int wat = 0;
    //     long yeet0 = 0;
    //     long yeet1 = 0;
    //     for (Entry<Mutf8Slice, HashMap<NameDescPair, InheritanceGroup>> a : imap.inheritanceMap.entrySet()) {
    //         for (Entry<NameDescPair, InheritanceGroup> ig : a.getValue().entrySet()) {
    //             int j = 0;
    //             InheritanceGroup ig0 = ig.getValue();
    //             while (ig0.down != null) {
    //                 ++j;
    //                 ig0 = ig0.down;
    //             }
    //             i = Math.max(i, j);
    //             // if (j > 1) {
    //                 yeet0 += j;
    //                 yeet1 += 1;
    //             // }
    //             if (j == 1702) {
    //                 ++wat;
    //                 System.out.println(ig.getValue().id);
    //                 System.out.println(a.getKey());
    //                 System.out.println(ig.getKey());
    //                 // System.out.println();
    //             }
    //         }
    //     }
    //     System.out.println(i);
    //     System.out.println(wat);
    //     System.out.println(yeet0 / (double) yeet1);
    //     Path inter = Obtainor.getInt();
    //     MemoryMappingTree tree = new MemoryMappingTree();
    //     MappingReader.read(inter, MappingFormat.TINY, tree);
    //     start = System.nanoTime();
    //     MappingIoMappings m = new MappingIoMappings(tree, tree.getNamespaceId("official"), tree.getNamespaceId("intermediary"), imap);
    //     end = System.nanoTime();
    //     System.out.println(new BigDecimal(end - start).divide(new BigDecimal(1000000000)));
    //     System.out.println(m.methodMap.size());
    // }
}
