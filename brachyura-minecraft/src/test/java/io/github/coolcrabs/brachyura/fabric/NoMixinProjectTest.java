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
            // Seems to work accross java versions for now
            TestUtil.assertSha256(b.jar, "fdfeb367b463839e2207b0c8bc887f1879b2157da838e51b1b0e98487c86e6ce");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
