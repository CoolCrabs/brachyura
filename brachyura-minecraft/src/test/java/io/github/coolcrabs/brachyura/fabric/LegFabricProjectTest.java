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
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class LegFabricProjectTest {
    
    SimpleFabricProject fabricProject = new SimpleFabricProject() {
        static final String LEG_FABRIC_MAVEN = "https://repo.legacyfabric.net/repository/legacyfabric/";

        @Override
        public VersionMeta createMcVersion() {
            return Minecraft.getVersion("1.8.9");
        }

        @Override
        protected FabricContext createContext() {
            return new SimpleFabricContext() {
                @Override
                protected MappingTree createIntermediary() {
                    MappingTree tree = Intermediary.ofMaven(LEG_FABRIC_MAVEN, new MavenId("net.legacyfabric", "intermediary", "1.8.9")).tree;
                    return tree;
                };
            };
        };

        @Override
        public MappingTree createMappings() {
            MappingTree tree = Yarn.ofMaven(LEG_FABRIC_MAVEN, new MavenId("net.legacyfabric:yarn:1.8.9+build.451")).tree;
            tree.getClass("net/minecraft/world/chunk/ServerChunkProvider", tree.getNamespaceId(Namespaces.NAMED)).getMethod("method_3864", "(II)Z", tree.getNamespaceId(Namespaces.INTERMEDIARY)).setDstName("chunkExists", tree.getNamespaceId(Namespaces.NAMED));
            return tree;
        }

        @Override
        public FabricLoader getLoader() {
            return new FabricLoader(FabricMaven.URL, FabricMaven.loader("0.12.5"));
        }

        @Override
        public Path getProjectDir() {
            Path result = TestUtil.ROOT.resolve("test").resolve("fabric").resolve("legfabric");
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
            if (JvmUtil.CURRENT_JAVA_VERSION == 8) // TestMod.java produces different cp order in j8 and j17
                TestUtil.assertSha256(b.jar, "09b408c35e71afd037d9eefae98bdb8b5bc64320989b798dad4aa5b96594c52d");
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
}
