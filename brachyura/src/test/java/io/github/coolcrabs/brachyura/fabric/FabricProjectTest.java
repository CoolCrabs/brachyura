package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.util.PathUtil;
import net.fabricmc.mappingio.tree.MappingTree;

class FabricProjectTest {
    FabricProject fabricProject = new FabricProject() {
        @Override
        public String getModId() {
            return "brachyuratestmod";
        }

        @Override
        public String getVersion() {
            return "1.0.0";
        }

        @Override
        public String getMcVersion() {
            return "1.16.5";
        }

        @Override
        public MappingTree getMappings() {
            MappingTree tree = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("1.16.5+build.10")).tree;
            return tree;
        }

        @Override
        public FabricLoader getLoader() {
            return new FabricLoader(FabricMaven.URL, FabricMaven.loader("0.11.6"));
        }

        @Override
        public Path getProjectDir() {
            Path result = PathUtil.CWD.getParent().resolve("testmod");
            assertTrue(Files.isDirectory(result)); 
            return result;
        }

        @Override
        public List<JavaJarDependency> createModDependencies() {
            return Collections.singletonList(
                Maven.getMavenJarDep(
                    FabricMaven.URL,
                    new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-resource-loader-v0", "0.4.2+ca58154a7d")
                )
            );
        }
        
    };

    @Disabled("Too slow for ci :(")
    @Test
    void testProject() {
        assertTrue(Files.isRegularFile(fabricProject.intermediaryjar.get().jar));
        assertTrue(Files.isRegularFile(fabricProject.namedJar.get().jar));
        assertTrue(Files.isRegularFile(fabricProject.getDecompiledJar().jar));
    }
    
    @Disabled("Too slow for ci :(")
    @Test
    void vscode() {
        //Todo better api for this?
        fabricProject.getTasks(p -> {
            if (p.name.equals("vscode")) p.doTask(new String[]{});
        });
    }

    @Test
    void compile() {
        assertTrue(fabricProject.build());
    }
}
