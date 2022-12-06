package io.github.coolcrabs.brachyura.recombobulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.recombobulator.attribute.Attribute;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeCode;
import io.github.coolcrabs.brachyura.recombobulator.attribute.AttributeRecord;
import io.github.coolcrabs.brachyura.recombobulator.attribute.Attributes;
import io.github.coolcrabs.brachyura.recombobulator.attribute.RecordComponentInfo;

public class SimpleTest {
    double yeet = 5;
    double bruh = 3;

    RecombobulatorOptions eager = new RecombobulatorOptions();
    RecombobulatorOptions lazy = new RecombobulatorOptions();

    public SimpleTest() {
        lazy.lazyAttributes = true;
    }

    @Disabled
    @Test
    void test() throws Exception {
        try {
            yeet += 3;
            Path pathToRead = getFileURIFromResources("io/github/coolcrabs/brachyura/recombobulator/SimpleTest.class");
            try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(pathToRead, EnumSet.of(StandardOpenOption.READ))) {
                MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                ClassInfo transformer = new ClassInfo(mappedByteBuffer, eager);
                System.out.println(transformer);
                System.out.println(transformer.byteSize());
                assertEquals(fileChannel.size(), transformer.byteSize());
                for (ConstantPoolEntry e : transformer.pool.pool) {
                    if (e instanceof ConstantUtf8) {
                        System.out.println(((ConstantUtf8)e).slice);
                    }
                }
                byte[] orig = new byte[(int)fileChannel.size()];
                mappedByteBuffer.get(orig);
                ByteBuffer b = ByteBuffer.wrap(new byte[transformer.byteSize()]);
                transformer.write(RecombobulatorOutput.of(b));
                byte[] newa = b.array();
                assertEquals(orig.length, newa.length);
                for (int i = 0; i < orig.length; i++) {
                    if (orig[i] != newa[i]) {
                        throw new RuntimeException("" + i);
                    }
                }
        }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    void testMc() throws IOException {
        Path bruh = Obtainor.getMc();
        try (FileSystem fs = FileSystemUtil.newJarFileSystem(bruh)) {
            Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".class")) {
                        // System.out.println(file.toString());
                        byte[] a = Files.readAllBytes(file);
                        ByteBuffer b = ByteBuffer.wrap(a);
                        ClassInfo ci = new ClassInfo(b, eager);
                        // if ("/awn.class".equals(file.toString())) findBadAttribute(b);
                        assertEquals(a.length, ci.byteSize());
                        ByteBuffer c = ByteBuffer.allocate(ci.byteSize());
                        ci.write(new DebugOutput(c, file.toString()));
                        byte[] d = c.array();
                        assertEquals(a.length, d.length);
                        for (int i = 0; i < a.length; i++) {
                            if (a[i] != d[i]) {
                                throw new RuntimeException(file + " " + i);
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    void findBadAttribute(ByteBuffer a) {
        ClassInfo ci = new ClassInfo(a, lazy);
        List<Attributes> al = new ArrayList<>();
        al.add(ci.attributes);
        for (MethodInfo m : ci.methods) al.add(m.attributes);
        for (FieldInfo f : ci.fields) al.add(f.attributes);
        for (int bruh = 0; bruh < al.size(); bruh++) { // enhanced for doesn't let us tack stuff on at the end
            Attributes ats = al.get(bruh);
            for (int i = 0; i < ats.size(); i++) {
                Attribute at;
                try {
                    at = ats.get(i);
                } catch (Exception e) {
                    at = ats.get(i);
                    throw e;
                }
                if (ci.byteSize() != a.remaining()) {
                    throw new RuntimeException("Index: " + i + " Name: " + ((ConstantUtf8)ci.pool.getEntry(at.attribute_name_index)).slice);
                }
                {
                    ByteBuffer out = ByteBuffer.allocate(ci.byteSize());
                    ci.write(RecombobulatorOutput.of(out));
                    for (int j = 0; j < a.remaining(); j++) {
                        if (a.get(j) != out.get(j)) {
                            throw new RuntimeException("Index: " + i + " Name: " + ((ConstantUtf8)ci.pool.getEntry(at.attribute_name_index)).slice);
                        }
                    }
                }
                if (at instanceof AttributeCode) {
                    al.add(((AttributeCode)at).attributes);
                }
                if (at instanceof AttributeRecord) {
                    for (RecordComponentInfo rci : ((AttributeRecord)at).components) {
                        al.add(rci.attributes);
                    }
                }
            }
        }
    }

    Path getFileURIFromResources(String fileName) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        return Paths.get(classLoader.getResource(fileName).toURI());
    }
}
