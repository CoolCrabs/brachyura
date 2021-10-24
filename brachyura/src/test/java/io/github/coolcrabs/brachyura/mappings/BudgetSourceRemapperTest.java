package io.github.coolcrabs.brachyura.mappings;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.fabric.FabricMaven;
import io.github.coolcrabs.brachyura.fabric.Yarn;
import io.github.coolcrabs.brachyura.util.StreamUtil;
import io.github.coolmineman.trieharder.FindReplaceSourceRemapper;
import net.fabricmc.mappingio.tree.MappingTree;

class BudgetSourceRemapperTest {
    @Test
    void smallYarn() throws Exception {
        MappingTree tree = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("1.16.5+build.10")).tree;
        FindReplaceSourceRemapper remapper = new FindReplaceSourceRemapper(tree, tree.getNamespaceId(Namespaces.INTERMEDIARY), tree.getNamespaceId(Namespaces.NAMED));
        try (InputStream is = getClass().getResourceAsStream("/PlantInAJar1_16_Intermediary.java")) {
            long start = System.currentTimeMillis();
            String remapped = remapper.remapString(StreamUtil.readFullyAsString(is));
            System.out.println(remapped);
            long end = System.currentTimeMillis() - start;
            System.out.println("Took " + end);
            assertFalse(remapped.contains("class_"));
            assertFalse(remapped.contains("method_"));
            assertFalse(remapped.contains("field_"));
        }
    }

    @Test
    void smallYarn2() throws Exception {
        MappingTree tree = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("1.16.5+build.10")).tree;
        FindReplaceSourceRemapper remapper = new FindReplaceSourceRemapper(tree, tree.getNamespaceId(Namespaces.INTERMEDIARY), tree.getNamespaceId(Namespaces.NAMED));
        try (InputStream is = getClass().getResourceAsStream("/PlantInAJar1_16_Intermediary.java")) {
            long start = System.currentTimeMillis();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String remapped = remapper.remapString(line);
                    System.out.println(remapped);
                }
            }
            long end = System.currentTimeMillis() - start;
            System.out.println("Took " + end);
        }
    }

    @Test
    void microYarn() throws Exception {
        MappingTree tree = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("1.16.5+build.10")).tree;
        FindReplaceSourceRemapper remapper = new FindReplaceSourceRemapper(tree, tree.getNamespaceId(Namespaces.INTERMEDIARY), tree.getNamespaceId(Namespaces.NAMED));
        System.out.println(remapper.remapString("class_1792"));
    }
}
