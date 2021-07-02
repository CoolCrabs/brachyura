package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import net.fabricmc.mappingio.tree.MappingTree;

class FabricProjectTest {
    @Test
    void testProject() {
        FabricProject fabricProject = new FabricProject() {
            @Override
            String getMcVersion() {
                return "1.16.5";
            }

            @Override
            MappingTree getMappings() {
                MappingTree tree = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("1.16.5+build.10")).tree;
                return tree;
            }
            
        };
        assertTrue(Files.isRegularFile(fabricProject.getIntermediaryJar().jar));
        assertTrue(Files.isRegularFile(fabricProject.getNamedJar().jar));
        assertTrue(Files.isRegularFile(fabricProject.getDecompiledJar()));
    }
}
