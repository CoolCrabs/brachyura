package io.github.coolcrabs.majoidea;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

class MajoideaTest {
    static Path homedir = Paths.get(System.getProperty("user.home"));
    static Path intermediary1_16_5 = homedir.resolve(".brachyura/cache/fabric/intermediary/1.16.5-intermediary-91BC6B95CCC43E07496D2A2DBD0EDA18720B0515C162F7F690D4BC880837D80A.jar");
    static MappingTree mappingTree;

    static {
        assertDoesNotThrow(() -> {
            try (InputStream is = MajoideaTest.class.getResourceAsStream("/yarn-1.16.5+build.10-merged.tiny")) {
                MemoryMappingTree mappingTree2 = new MemoryMappingTree();
                MappingReader.read(new InputStreamReader(is), MappingFormat.TINY_2, mappingTree2);
                mappingTree = mappingTree2;
            }
        });
    }

    @Test
    void test() {
        assertDoesNotThrow(() -> {
            Path mod = Paths.get("./testjars/test1-h2or.jar");
            try (FileSystem fileSystem = FileSystemUtil.newJarFileSystem(mod)) {
                try (Majoidea majoidea = new Majoidea(fileSystem.getPath("/"), null, Collections.singletonList(intermediary1_16_5))) {
                    majoidea.remap(mappingTree, mappingTree.getNamespaceId("intermediary"), mappingTree.getNamespaceId("named"));
                    majoidea.run();
                }
            }
        });
    }
}
