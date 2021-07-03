package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.util.PathUtil;
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

            @Override
            JavaJarDependency getLoader() {
                return Maven.getMavenJarDep(FabricMaven.URL, FabricMaven.loader("0.11.6"));
            }

            @Override
            Path getProjectDir() {
                Path result = PathUtil.CWD.getParent().resolve("testmod");
                assertTrue(Files.isDirectory(result)); 
                return result;
            }
            
        };
        assertTrue(Files.isRegularFile(fabricProject.getIntermediaryJar().jar));
        assertTrue(Files.isRegularFile(fabricProject.getNamedJar().jar));
        assertTrue(Files.isRegularFile(fabricProject.getDecompiledJar()));
        fabricProject.vscode();
    }
}
