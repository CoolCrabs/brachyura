package io.github.coolcrabs.brachyura.decompiler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.decompiler.LineNumberTableReplacer.ClassFile;
import io.github.coolcrabs.brachyura.util.StreamUtil;

class LineNumberTableReplacerTest {
    final double a = 5;

    @Test
    void readSelf() throws Exception {
        try (DataInputStream in = new DataInputStream(this.getClass().getClassLoader().getResourceAsStream("io/github/coolcrabs/brachyura/decompiler/LineNumberTableReplacerTest.class"))) {
            ClassFile classFile = LineNumberTableReplacer.readClassFile(in);
            double test = 5;
            System.out.println(test);
            System.out.println(a);
            System.out.println(classFile);
        }
    }

    @Test
    void roundTripSelf() throws Exception {
        byte[] expected;
        try (DataInputStream in = new DataInputStream(this.getClass().getClassLoader().getResourceAsStream("io/github/coolcrabs/brachyura/decompiler/LineNumberTableReplacerTest.class"))) {
            expected = StreamUtil.readFullyAsBytes(in);
        }
        try (DataInputStream in = new DataInputStream(this.getClass().getClassLoader().getResourceAsStream("io/github/coolcrabs/brachyura/decompiler/LineNumberTableReplacerTest.class"))) {
            ClassFile classFile = LineNumberTableReplacer.readClassFile(in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream out0 = new DataOutputStream(out);
            LineNumberTableReplacer.writeClassFile(out0, classFile);
            byte[] actual = out.toByteArray();
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], actual[i], "Byte " + i);
            }
        }
    }
}
