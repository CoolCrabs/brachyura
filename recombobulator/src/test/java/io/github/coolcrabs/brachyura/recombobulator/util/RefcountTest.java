package io.github.coolcrabs.brachyura.recombobulator.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.recombobulator.ClassInfo;
import io.github.coolcrabs.brachyura.recombobulator.ConstantPoolEntry;
import io.github.coolcrabs.brachyura.recombobulator.ConstantUtf8;
import io.github.coolcrabs.brachyura.recombobulator.FileSystemUtil;
import io.github.coolcrabs.brachyura.recombobulator.Obtainor;
import io.github.coolcrabs.brachyura.recombobulator.RecombobulatorOptions;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Attribute;

public class RefcountTest {
    @Test
    void refcount() throws IOException {
        RecombobulatorOptions options = new RecombobulatorOptions();
        options.lazyAttributes = true;
        Path bruh = Obtainor.getMc();
        try (FileSystem fs = FileSystemUtil.newJarFileSystem(bruh)) {
            Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".class")) {
                        // System.out.println(file.toString());
                        byte[] a = Files.readAllBytes(file);
                        ByteBuffer b = ByteBuffer.wrap(a);
                        ClassInfo ci = new ClassInfo(b, options);
                        ConstantPoolRefCounter refs = new ConstantPoolRefCounter(ci.pool);
                        ci.accept(refs);
                        for (int i = 1; i <= ci.pool.size(); i++) {
                            if (refs.getRef(i) == 0) {
                                ConstantPoolEntry e = ci.pool.getEntry(i);
                                String error = i + " " + e.toString();
                                if (e instanceof ConstantUtf8) {
                                    error += " ";
                                    error += ((ConstantUtf8)e).slice.toString();
                                }
                                throw new RuntimeException(error);
                            }
                        }
                        int bruh = 0;
                        for (Attribute as : ci.attributes) bruh++;
                        assertEquals(ci.attributes.size(), bruh);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
