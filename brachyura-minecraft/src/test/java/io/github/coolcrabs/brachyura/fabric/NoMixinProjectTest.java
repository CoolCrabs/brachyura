package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.TestUtil;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyCollector;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import net.fabricmc.mappingio.tree.MappingTree;

public class NoMixinProjectTest {
    SimpleFabricProject fabricProject = new SimpleFabricProject() {
        @Override
        public VersionMeta createMcVersion() {
            return Minecraft.getVersion("1.16.5");
        }

        @Override
        public MappingTree createMappings() {
            return createMojmap();
        }

        @Override
        public FabricLoader getLoader() {
            return new FabricLoader(FabricMaven.URL, FabricMaven.loader("0.12.12"));
        }

        @Override
        public Path getProjectDir() {
            Path result = TestUtil.ROOT.resolve("test").resolve("fabric").resolve("mojmap_nomixin");
            assertTrue(Files.isDirectory(result)); 
            return result;
        }

        @Override
        public void getModDependencies(ModDependencyCollector d) {
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
            if (JvmUtil.CURRENT_JAVA_VERSION == 8)
                TestUtil.assertSha256(b.jar, "680f6122e8ef0a9f8f42f14189d7ca7e2c0bf252674af89614c5762366d7f748");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
