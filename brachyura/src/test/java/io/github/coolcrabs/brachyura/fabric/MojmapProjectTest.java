package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.cfr.CfrDecompiler;
import io.github.coolcrabs.brachyura.decompiler.fernflower.FernflowerDecompiler;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyCollector;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyFlag;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import net.fabricmc.mappingio.tree.MappingTree;

class MojmapProjectTest {
    SimpleFabricProject fabricProject = new SimpleFabricProject() {
        @Override
        public VersionMeta createMcVersion() {
            return Minecraft.getVersion("1.19-pre1");
        }

        public int getJavaVersion() {
            return 17;
        };

        @Override
        public MappingTree createMappings() {
            return createMojmap();
        }

        @Override
        public FabricLoader getLoader() {
            return new FabricLoader(FabricMaven.URL, FabricMaven.loader("0.14.6"));
        }

        @Override
        public Path getProjectDir() {
            Path result = PathUtil.CWD.getParent().resolve("test").resolve("fabric").resolve("mojmap");
            assertTrue(Files.isDirectory(result)); 
            return result;
        }

        @Override
        public void getModDependencies(ModDependencyCollector d) {
            jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-resource-loader-v0", "0.5.0+4a05de7f73"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
            jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-game-rule-api-v1", "1.0.16+ec94c6f673"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
            jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-registry-sync-v0", "0.9.12+56447d9b73"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
            jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-api-base", "0.4.7+f71b366f73"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
            jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-networking-api-v1", "1.0.24+f71b366f73"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
            jij(d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.ini4j:ini4j:0.5.4"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
        };

        @Override
        public BrachyuraDecompiler decompiler() {
            return null;
            // return new CfrDecompiler();
            // return new FernflowerDecompiler(Maven.getMavenJarDep("https://maven.quiltmc.org/repository/release", new MavenId("org.quiltmc:quiltflower:1.7.0"))); 
        };
    };

    @Test
    void testProject() {
        assertTrue(Files.isRegularFile(fabricProject.context.get().intermediaryjar.get().jar));
        assertTrue(Files.isRegularFile(fabricProject.context.get().namedJar.get().jar));
        // assertTrue(Files.isRegularFile(fabricProject.getDecompiledJar().jar));
    }
    
    @Test
    void ide() {
        long a = System.currentTimeMillis();
        //Todo better api for this?
        fabricProject.getTasks(p -> {
            if (p.name.equals("netbeans")) p.doTask(new String[]{});
            if (p.name.equals("idea")) p.doTask(new String[]{});
            if (p.name.equals("jdt")) p.doTask(new String[]{});
        });
        long b = System.currentTimeMillis();
        System.out.println(b - a);
    }

    @Test
    void compile() {
        if (JvmUtil.CURRENT_JAVA_VERSION >= 16) {
            try {
                fabricProject.build();
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}
