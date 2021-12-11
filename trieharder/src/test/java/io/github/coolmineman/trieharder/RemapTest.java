package io.github.coolmineman.trieharder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

class RemapTest {
    static MappingTree mappings;
    static FindReplaceSourceRemapper remapper;
    
    static {
        MemoryMappingTree m = new MemoryMappingTree(true);
        assertDoesNotThrow(() -> {
            try (Reader r = new InputStreamReader(RemapTest.class.getResourceAsStream("/mappings.tiny"))) {
                MappingReader.read(r, MappingFormat.TINY_2, m);
            }
        });
        mappings = m;
        remapper = new FindReplaceSourceRemapper(mappings, mappings.getNamespaceId("intermediary"), mappings.getNamespaceId("named"));
    }

    @Test
    void test() throws IOException {
        try (Reader in = new InputStreamReader(RemapTest.class.getResourceAsStream("/PlantInAJar1_16_Intermediary.java"))) {
            long start = System.currentTimeMillis();
            StringWriter w = new StringWriter();
            remapper.remap(in, w);
            String remapped = w.toString();
            System.out.println(remapped);
            long end = System.currentTimeMillis() - start;
            System.out.println("Took " + end);
            assertFalse(remapped.contains("class_"));
            assertFalse(remapped.contains("method_"));
            assertFalse(remapped.contains("field_"));
        }
    }

    @Test
    void advancedTest() throws IOException {
        try (Reader in = new InputStreamReader(RemapTest.class.getResourceAsStream("/Bruh.java"))) {
            long start = System.currentTimeMillis();
            StringWriter w = new StringWriter();
            remapper.remap(in, w);
            String remapped = w.toString();
            System.out.println(remapped);
            long end = System.currentTimeMillis() - start;
            System.out.println("Took " + end);
            assertEquals(
                "import net.minecraft.util.registry.Registry;\n\npublic class Bruh {\n    String a = \"class_2378\"; // class_2378\n    /* class_2378 */\n    Registry b;\n    String c = \"\"\"\n               class_2378\\n\"\"\";\n    Registry d;\n}\n",
                remapped
            );
        }
    }

    @Test
    void bruh() throws IOException {
        try (Reader in = new InputStreamReader(RemapTest.class.getResourceAsStream("/ModNioResourcePack.java"))) {
            long start = System.currentTimeMillis();
            StringWriter w = new StringWriter();
            remapper.remap(in, w);
            String remapped = w.toString();
            System.out.println(remapped);
            long end = System.currentTimeMillis() - start;
            System.out.println("Took " + end);
            assertFalse(remapped.contains("class_"));
            assertFalse(remapped.contains("method_"));
            assertFalse(remapped.contains("field_"));
        }
    }

    @Test
    void ahh() throws IOException {
        String bruh = remapper.remapString(new String(Base64.getDecoder().decode("IlwiIiBtZXRob2RfMTQzOTM="), StandardCharsets.UTF_8));
        System.out.println(bruh);
        assertFalse(bruh.contains("method_"));
    }
}
