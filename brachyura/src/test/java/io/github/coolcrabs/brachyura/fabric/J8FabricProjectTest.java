package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.TestUtil;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyCollector;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyFlag;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import net.fabricmc.mappingio.tree.MappingTree;
import org.junit.jupiter.api.Disabled;

class J8FabricProjectTest {
    SimpleFabricProject fabricProject = new SimpleFabricProject() {
        @Override
        public VersionMeta createMcVersion() {
            return Minecraft.getVersion("1.16.5");
        }

        @Override
        public MappingTree createMappings() {
            MappingTree tree = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("1.16.5+build.10")).tree;
            return tree;
        }

        @Override
        public FabricLoader getLoader() {
            return new FabricLoader(FabricMaven.URL, FabricMaven.loader("0.12.5"));
        }

        @Override
        public Path getProjectDir() {
            Path result = PathUtil.CWD.getParent().resolve("testmod");
            assertTrue(Files.isDirectory(result)); 
            return result;
        }

        @Override
        public void getModDependencies(ModDependencyCollector d) {
            jij(d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.ini4j:ini4j:0.5.4"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
            jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-resource-loader-v0", "0.4.8+3cc0f0907d"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
            jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-game-rule-api-v1", "1.0.7+3cc0f0907d"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
        };

       @Override
       public BrachyuraDecompiler decompiler() {
           return null;
       };
    };

    @Test
    void compile() {
        try {
            long s = System.currentTimeMillis();
            JavaJarDependency b = fabricProject.build();
            long s2 = System.currentTimeMillis() - s;
            System.out.println(s2);
            if (JvmUtil.CURRENT_JAVA_VERSION == 8) // TestMod.java produces different cp order in j8 and j17
                TestUtil.assertSha256(b.jar, "e0dbaa897a5f86f77e3fec2a7fd43dbf4df830a10b9c75dd0ae23f28e5da3c67");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    void ide() {
        long a = System.currentTimeMillis();
        //Todo better api for this?
        fabricProject.getTasks(p -> {
            try {
                if (p.name.equals("netbeans")) p.doTask(new String[]{});
                if (p.name.equals("idea")) p.doTask(new String[]{});
                if (p.name.equals("jdt")) p.doTask(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        });
        long b = System.currentTimeMillis();
        System.out.println(b - a);
    }
    
    @Disabled
    @Test
    void bruh() {
        fabricProject.getTasks(p -> {
            System.out.println(p.name);
            if (p.name.equals("runMinecraftClient"))
                try {
                    p.doTask(new String[]{}); 
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }

        });
    }
}
