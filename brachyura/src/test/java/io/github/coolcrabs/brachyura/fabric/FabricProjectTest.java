package io.github.coolcrabs.brachyura.fabric;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.cfr.CfrDecompiler;
import io.github.coolcrabs.brachyura.decompiler.fernflower.FernflowerDecompiler;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerVisitor;
import net.fabricmc.mappingio.tree.MappingTree;

class FabricProjectTest {
    FabricProject fabricProject = new FabricProject() {
        @Override
        public String getMcVersion() {
            return "21w39a";
        }

        public int getJavaVersion() {
            return 17;
        };

        public Consumer<AccessWidenerVisitor> getAw() {
            return (v) -> {
                try {
                    new AccessWidenerReader(v).read(Files.newBufferedReader(getResourcesDir().resolve("testaw.accesswidener")), Namespaces.NAMED);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        };

        @Override
        public MappingTree createMappings() {
            MappingTree tree = Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn("21w39a+build.3")).tree;
            return tree;
        }

        @Override
        public FabricLoader getLoader() {
            return new FabricLoader(FabricMaven.URL, FabricMaven.loader("0.12.12"));
        }

        @Override
        public Path getProjectDir() {
            Path result = PathUtil.CWD.getParent().resolve("testmod");
            assertTrue(Files.isDirectory(result)); 
            return result;
        }

        @Override
        public void getModDependencies(ModDependencyCollector d) {
            d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-resource-loader-v0", "0.4.8+a00e834b88"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE, ModDependencyFlag.JIJ);
            d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-game-rule-api-v1", "1.0.7+cbda931888"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE, ModDependencyFlag.JIJ);
            d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.ini4j:ini4j:0.5.4"), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE, ModDependencyFlag.JIJ);
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
        assertTrue(Files.isRegularFile(fabricProject.intermediaryjar.get().jar));
        assertTrue(Files.isRegularFile(fabricProject.namedJar.get().jar));
        // assertTrue(Files.isRegularFile(fabricProject.getDecompiledJar().jar));
    }
    
    @Test
    void ide() {
        long a = System.currentTimeMillis();
        //Todo better api for this?
        fabricProject.getTasks(p -> {
            if (p.name.equals("vscode")) p.doTask(new String[]{});
            if (p.name.equals("netbeans")) p.doTask(new String[]{});
            if (p.name.equals("idea")) p.doTask(new String[]{});
            if (p.name.equals("eclipse")) p.doTask(new String[]{});
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
